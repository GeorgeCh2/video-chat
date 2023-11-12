import React, { useContext } from "react";
import { Grid, Typography, Paper, makeStyles } from '@material-ui/core'

import { SocketContext } from "../SocketContext";

const useStyles = makeStyles((theme) => ({
    video: {
        width: '550px',
        [theme.breakpoints.down('xs')]: {
            width: '300px'
        }
    },
    gridContainer: {
        justifyContent: 'center',
        [theme.breakpoints.down('xs')]: {
          flexDirection: 'column',
        },
      },
      paper: {
        padding: '10px',
        border: '2px solid black',
        margin: '10px',
      },
}));

const VideoPlayer = () => {
    const { name, otherName, videoStream, otherJoined, myVideo, otherVideo } = useContext(SocketContext);
    const classes = useStyles();

    return (
        <Grid container className={classes.gridContainer}>
            {videoStream && (
                <Paper className={classes.paper}>
                    <Grid item xs={12} md={6}>
                        <Typography variant="h5" gutterBottom>{name || 'Name'}</Typography>
                        <video playsInline muted ref={myVideo} autoPlay className={classes.video} />
                    </Grid>
                </Paper>
            )}

            {otherJoined && (
                <Paper className={classes.paper}>
                    <Grid item xs={12} md={6}>
                        <Typography variant="h5" gutterBottom>{otherName || 'Name'}</Typography>
                        <video playsInline muted ref={otherVideo} autoPlay className={classes.video} />
                    </Grid>
                </Paper>
            )}
        </Grid>
    );
};

export default VideoPlayer;