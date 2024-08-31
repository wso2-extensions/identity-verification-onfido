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
    Box,
    Menu,
    MenuItem,
    Avatar,
    Typography,
} from "@oxygen-ui/react";
import guardioLogo from "../images/guardio-life-horizontal.webp";
import avatarImage from "../images/avatar.png";
import { UserPlusIcon, ArrowRightToBracketIcon } from "@oxygen-ui/react-icons";
import { UserIcon, ArrowRightFromBracketIcon } from "@oxygen-ui/react-icons";

export const NavBar = () => {
    const { state, signIn, signOut } = useAuthContext();
    const navigate = useNavigate();
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

    const handleLogin = () => {
        signIn().catch((e) => console.log("Something went wrong while signing in. ", e));
    };

    const handleRegister = () => {
        navigate("/register");
    };

    const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleLogout = () => {
        signOut().catch((e) => console.log("Something went wrong while signing out. ", e));
        handleClose();
    };

    return (
        <AppBar position="sticky" color="default" elevation={0} sx={{ 
            borderBottom: (theme) => `1px solid ${theme.palette.divider}`, 
            backgroundColor: '#F8F9FA'
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
                    {['Overview', 'Partners', 'Pricing', 'About Us', 'Contact'].map((item) => (
                        <Button key={item} color="inherit" sx={{ mx: 1, color: 'text.secondary' }}>
                            {item}
                        </Button>
                    ))}
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {!state?.isAuthenticated ? (
                        <>
                            <Button 
                                color="primary" 
                                variant="outlined" 
                                onClick={handleRegister} 
                                startIcon={<UserPlusIcon />}
                                sx={{ mr: 2 }}
                            >
                                Register
                            </Button>
                            <Button 
                                color="primary" 
                                variant="contained" 
                                onClick={handleLogin}
                                startIcon={<ArrowRightToBracketIcon />}
                            >
                                Login
                            </Button>
                        </>
                    ) : (
                        <>
                            <Box sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={handleMenu}>
                                <Typography variant="body2" sx={{ ml: 1 }}>{state.username}</Typography>
                                <Avatar 
                                    alt="User Avatar" 
                                    src={avatarImage} 
                                    sx={{ 
                                        width: 35, 
                                        height: 38.2, 
                                        border: '1px solid #bdbdbd',
                                        ml: 1
                                    }} 
                                />
                            </Box>
                            <Menu
                                anchorEl={anchorEl}
                                open={Boolean(anchorEl)}
                                onClose={handleClose}
                                anchorOrigin={{
                                    vertical: 'bottom',
                                    horizontal: 'right',
                                }}
                                transformOrigin={{
                                    vertical: 'top',
                                    horizontal: 'right',
                                }}
                                slotProps={{
                                    paper: {
                                        sx: {
                                            width: '200px',
                                            boxShadow: '0px 2px 8px rgba(0,0,0,0.15)',
                                            borderRadius: '8px',
                                            mt: 1
                                        }
                                    }
                                }}
                            >
                                <MenuItem onClick={handleClose} sx={{ py: 1.5 }}>
                                    <Box sx={{ mr: 2 }}>
                                        <UserIcon />
                                    </Box>
                                    <Typography variant="body2" sx={{ flexGrow: 1 }}>My Account</Typography>
                                </MenuItem>
                                <MenuItem onClick={handleLogout} sx={{ py: 1.5 }}>
                                    <Box sx={{ mr: 2 }}>
                                        <ArrowRightFromBracketIcon />
                                    </Box>
                                    <Typography variant="body2" sx={{ flexGrow: 1 }}>Logout</Typography>
                                </MenuItem>
                            </Menu>
                        </>
                    )}
                </Box>
            </Toolbar>
        </AppBar>
    );
}
