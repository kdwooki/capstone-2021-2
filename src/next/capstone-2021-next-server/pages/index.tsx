import React from 'react';
import { useSelector } from 'react-redux';
import { AppNavigation } from "../src/components";
import { RootState } from "../src/modules";
import { Hub, Content, Dashboard } from '../src/container';
import {Button} from "@material-ui/core";

type Props = {

}

const Home = ({ }: Props) => {
    const appNav = useSelector((state: RootState) => state.AppNavReducer);
    return (
        <React.Fragment>
            <div className={"ovf"} style={{ display: 'flex', flexDirection: 'column', width: '100%', paddingRight: '16pt' }}>
                <AppNavigation />
                <div className={"ovf"} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%', width: '100%',
                    marginTop: '100pt', paddingTop: '8pt', overflowY: 'auto' }}>
                    {
                        appNav.value === 0 && <Hub />
                    }
                    {
                        appNav.value === 1 && <Dashboard />
                    }
                    {
                        appNav.value === 2 && <Content />
                    }
                </div>
            </div>
        </React.Fragment>
    )
}

export const getServerSideProps = async ({ apolloClient }) => {
    return { props: { } };
}

export default Home;