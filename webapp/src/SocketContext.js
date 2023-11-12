import React, {createContext, useState, useRef, useEffect } from "react";
import Peer from 'simple-peer';

const SocketContext = createContext();

const ws = new WebSocket('ws://127.0.0.1:8080/chat');

const ContextProvider = ({ children }) => {
    const [room, setRoom] = useState();
    const [userId, setUserId] = useState('');
    const [name, setName] = useState('');
    const [otherName, setOtherName] = useState('');
    const [otherJoined, setOtherJoined] = useState(false);
    const [videoStream, setVideoStream] = useState([]);
    const [screenStream, setScreenStream] = useState();
    const [meetEnded, setMeetEnded] = useState(true);

    const myVideo = useRef();
    const otherVideo = useRef();
    const myScreen = useRef();
    const connectionRef = useRef();

    useEffect(() => {
        navigator.mediaDevices.getUserMedia({ video: true, audio: false })
            .then((stream) => {
                setUserId((Math.random() + 1).toString(36).substring(7));
                setVideoStream(stream);

                myVideo.current.srcObject = stream;
            });
    }, []);

    // handle ws message
    ws.onmessage = ((event) => {
        onMessage(JSON.parse(event.data));
    });

    const joinRoom = () => {
        // send join room msg
        sendMsg('JOIN', null);

        setMeetEnded(false);
    };

    const leaveRoom = () => {
        connectionRef.current.destroy();

        // send leave msg
        sendMsg('LEAVE', null);
        setMeetEnded(true);
    };

    const presentScreen = (present) => {
        if (present && !myScreen.current.srcObject) {
            navigator.mediaDevices.getDisplayMedia({ video: true, audio: false })
                .then((stream) => {
                    setScreenStream(stream);

                    connectionRef.current.addStream(stream);

                    myScreen.current.srcObject = stream;
                });
        } else if (!present) {
            // stop share screen
            connectionRef.current.removeStream(screenStream);
            setScreenStream(null);
            myScreen.current.srcObject = null;
        }
    };

    const sendPeerData = () => {
        const peer = new Peer({ initiator: true, trickle: false, stream: videoStream });

        peer.on('signal', (data) => {
            sendMsg('PROTOCOL', JSON.stringify({ type: 'new_join', signalData: data }));
        });

        peer.on('stream', (stream) => {
            otherVideo.current.srcObject = stream;
        });

        connectionRef.current = peer;
    }

    const receivePeerData = (signalData) => {
        const peer = new Peer({ initiator: false, trickle: false, stream: videoStream });

        peer.on('signal', (data) => {
            sendMsg('PROTOCOL', JSON.stringify({ type: 'accept_peer', signalData: data }));
        });

        peer.on('stream', (stream) => {
            otherVideo.current.srcObject = stream;
        });

        peer.signal(signalData);

        connectionRef.current = peer;
    }

    const onMessage = (message) => {
        switch (message.msgType) {
            case 'JOIN': {
                // other user join the room
                setOtherJoined(true);
                setOtherName(message.name);
                break;
            }

            case 'LEAVE': {
                // TODO other user leave the room
                setOtherJoined(false);
                setOtherName('');
            }

            case 'PROTOCOL': {
                const msgData = JSON.parse(message.msg);
                switch (msgData.type) {
                    case 'send_peer': {
                        sendPeerData();
                        break;
                    }

                    case 'new_join': {
                        receivePeerData(msgData.signalData);
                        break;
                    }

                    case 'accept_peer': {
                        setOtherName(message.name);
                        setOtherJoined(true);
                        connectionRef.current.signal(msgData.signalData);
                        break;
                    }
                }
            }
        }
    };

    const sendMsg = (msgType, msg) => {
        ws.send(JSON.stringify({ roomId: room, uid: userId, msgType, name, msg}))
    };

    return (
        <SocketContext.Provider value={{
            myVideo,
            otherVideo,
            userId,
            name,
            otherName,
            room,
            otherJoined,
            videoStream,
            meetEnded,
            setName,
            setRoom,
            joinRoom,
            leaveRoom
        }}>
            {children}
        </SocketContext.Provider>
    );
};

export { ContextProvider, SocketContext }