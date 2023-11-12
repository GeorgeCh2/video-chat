import React from "react";
import { makeStyles } from '@material-ui/core/styles';

import VideoPlayer from "./components/VideoPlayer";
import SideBar from "./components/Sidebar";

const useStyles = makeStyles((theme) => ({
    wrapper: {
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      width: '100%',
    },
  }));

const App = () => {
    const classes = useStyles();

    return (
        <div className={classes.wrapper}>
            <VideoPlayer/>
            <SideBar/>
        </div>
    );
};

export default App;