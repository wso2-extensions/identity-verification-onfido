/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
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

import { useAuthContext } from "@asgardeo/auth-react";
import React, { FunctionComponent, ReactElement, useCallback, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Footer, LoadingSpinner, NavBar } from "../components";
import { Typography, Button, Box, Paper } from "@mui/material";
import backgroundImage from "../images/life-insurance-background.png";

export const LoginPage: FunctionComponent = (): ReactElement => {
    const { state, signIn } = useAuthContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (state?.isAuthenticated) {
            navigate("/");
        }
    }, [state?.isAuthenticated, navigate]);

    const handleLogin = useCallback(() => {
        signIn()
            .then(() => {
                console.log("Sign-in initiated successfully");
            })
            .catch((error) => {
                console.error("Error occurred while signing in:", error);
                navigate("/generic-error", { state: { message: "Error occurred while signing in" } });
            });
    }, [navigate, signIn]);

    if (state?.isLoading) {
        return <LoadingSpinner />;
    }

    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                flexDirection: 'column',
                backgroundImage: `url(${backgroundImage})`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
            }}
        >
            <NavBar />
            <Box
                sx={{
                    flexGrow: 1,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'flex-end',
                    padding: { xs: 2, sm: 4, md: 6 },
                }}
            >
                <Paper
                    elevation={3}
                    sx={{
                        p: 4,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        borderRadius: 2,
                        maxWidth: '400px',
                        width: '100%',
                    }}
                >
                    <Typography component="h1" variant="h4" sx={{ mb: 2, color: 'primary.main' }}>
                        Welcome to LifeGuardian
                    </Typography>
                    <Typography variant="subtitle1" sx={{ mb: 3, textAlign: 'center' }}>
                        Safeguarding Your Journey with Unwavering Commitment.
                    </Typography>
                    <Button
                        onClick={handleLogin}
                        variant="contained"
                        size="large"
                        fullWidth
                        sx={{ 
                            mt: 2, 
                            backgroundColor: 'primary.main',
                            '&:hover': {
                                backgroundColor: 'primary.dark',
                            },
                        }}
                    >
                        Login
                    </Button>
                </Paper>
            </Box>
            <Footer />
        </Box>
    );
};