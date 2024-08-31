/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from "react";
import { useAuthContext } from "@asgardeo/auth-react";
import { useNavigate } from "react-router-dom";
import {
    AppBar,
    Toolbar,
    Button,
    Box
} from "@oxygen-ui/react";
import guardioLogo from "../images/guardio-life-horizontal.webp";
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import LoginIcon from '@mui/icons-material/Login';

export const NavBar = () => {
    const { state, signIn } = useAuthContext();
    const navigate = useNavigate();

    const handleLogin = () => {
        signIn().catch((e) => console.log("Something went wrong while signing in. ", e));
    };

    const handleRegister = () => {
        navigate("/register");
    };

    return (
        <AppBar position="sticky" color="default" elevation={0} sx={{ 
            borderBottom: (theme) => `1px solid ${theme.palette.divider}`, 
            backgroundColor: 'white'
        }}>
            <Toolbar sx={{ justifyContent: 'space-between', padding: '0.5rem 2rem' }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <img 
                        src={guardioLogo}
                        alt="Guardio Life" 
                        style={{ height: '35px' }}
                    />
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1, justifyContent: 'center', padding: '0 2rem' }}>
                    <Button color="inherit" sx={{ mr: 2, color: '#66799e' }}>Overview</Button>
                    <Button color="inherit" sx={{ mr: 2, color: '#66799e' }}>Partners</Button>
                    <Button color="inherit" sx={{ mr: 2, color: '#66799e' }}>Pricing</Button>
                    <Button color="inherit" sx={{ mr: 2, color: '#66799e' }}>About Us</Button>
                    <Button color="inherit" sx={{ mr: 2, color: '#66799e' }}>Contact</Button>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {!state?.isAuthenticated && (
                        <>
                            <Button 
                                color="primary" 
                                variant="outlined" 
                                onClick={handleRegister} 
                                sx={{ 
                                    mr: 2
                                }}
                            >
                                <PersonAddIcon sx={{ mr: 1 }} />
                                Register
                            </Button>
                            <Button 
                                color="primary" 
                                variant="contained" 
                                onClick={handleLogin}
                            >
                                <LoginIcon sx={{ mr: 1 }} />
                                Login
                            </Button>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
}
