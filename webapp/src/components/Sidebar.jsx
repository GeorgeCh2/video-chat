import React, { useState, useContext } from "react";
import { Button, TextField, Grid, Container, Paper, makeStyles } from '@material-ui/core';

import { SocketContext } from '../SocketContext';

const useStyles = makeStyles((theme) => ({
    root: {
      display: 'flex',
      flexDirection: 'column',
    },
    gridContainer: {
      width: '100%',
      [theme.breakpoints.down('xs')]: {
        flexDirection: 'column',
      },
    },
    container: {
      width: '600px',
      margin: '35px 0',
      padding: 0,
      [theme.breakpoints.down('xs')]: {
        width: '80%',
      },
    },
    margin: {
      marginTop: 20,
    },
    padding: {
      padding: 20,
    },
    paper: {
      padding: '10px 20px',
      border: '2px solid black',
    },
  }));

  const SideBar = ({ children }) => {
    const { setRoom, setName, name, room, meetEnded, joinRoom, leaveRoom } = useContext(SocketContext);
    const classes = useStyles();

    return (
        <Container className={classes.container}>
            <Paper elevation={10} className={classes.paper}>
                <Grid container className={classes.gridContainer}>
                    <Grid item xs={12} md={6} className={classes.padding}>
                        <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} fullWidth />
                    </Grid>

                    <Grid item xs={12} md={6} className={classes.padding}>
                        <TextField label="RoomId" value={room} onChange={(e) => setRoom(e.target.value)} fullWidth />
                        {!meetEnded ? (
                            <Button variant="contained" color="secondary" fullWidth onClick={leaveRoom} className={classes.margin}>
                            Leave
                            </Button>
                        ) : (
                            <Button variant="contained" color="primary" fullWidth onClick={joinRoom} className={classes.margin}>
                            Join
                            </Button>
                        )}
                    </Grid>
                </Grid>
            </Paper>
        </Container>
    );
  };

  export default SideBar;