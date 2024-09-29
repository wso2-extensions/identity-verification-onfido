/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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
import React, { FunctionComponent, ReactElement, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Footer } from "../components/Footer";
import { NavBar } from "../components/NavBar";
import { LoadingSpinner } from "../components/LoadingSpinner";
import { Typography, Button, Box } from "@oxygen-ui/react";
import { handleMissingClientId } from "../util/IdvProviderMessages";
import { useConfig } from "../ConfigContext";

const GUARDIO_FAMILY_IMAGE = `/images/guardio-family.svg`;

export const LoginPage: FunctionComponent = (): ReactElement => {
    const { state, signIn } = useAuthContext();
    const navigate = useNavigate();
    const location = useLocation();
    const config = useConfig();

    useEffect(() => {
        if (state?.isAuthenticated) {
            const from = (location.state as any)?.from?.pathname || "/";
            navigate(from, { replace: true });
        }
    }, [state?.isAuthenticated, navigate, location]);

    if (!config?.clientID) {
        return handleMissingClientId();
    }

    if (state?.isLoading) {
        return <LoadingSpinner />;
    }

    const handleLogin = () => {
        signIn()
            .catch((error) => {
                console.error("Error during sign-in:", error);
                navigate('/auth-error');
            });
    };

    return (
        <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <NavBar />
            <Box sx={{ 
                flexGrow: 1, 
                display: 'flex', 
                flexDirection: { xs: 'column', md: 'row' },
                alignItems: 'center',
                justifyContent: 'center',
                padding: { xs: 2, sm: 4, md: 6 },
                maxWidth: '1400px',
                margin: '0 auto',
                width: '100%'
            }}>
                <Box sx={{ 
                    flex: 1,
                    display: { xs: 'none', md: 'flex' },
                    justifyContent: 'center',
                    alignItems: 'center',
                    maxWidth: '60%'
                }}>
                    <img src={GUARDIO_FAMILY_IMAGE} style={{ maxWidth: '100%', height: 'auto' }} alt="Guardio Family" />
                </Box>
                <Box sx={{ 
                    flex: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    maxWidth: { xs: '100%', md: '40%' },
                    pl: { md: 4 }
                }}>
                    <Typography variant="h3" component="h1" gutterBottom fontWeight="bold" sx={{ mb: 1 }}>
                        Welcome to Guardio Life
                    </Typography>
                    <Typography variant="h5" component="h2" gutterBottom color="text.secondary" sx={{ mb: 3 }}>
                        Secure Your Family&apos;s Future
                    </Typography>
                    <Typography variant="body1" paragraph sx={{ mb: 4, fontSize: '1.1rem' }}>
                        Log in to access your account and manage your insurance policies. We&apos;re here to protect what matters most to you.
                    </Typography>
                    <Button
                        onClick={handleLogin}
                        fullWidth
                        variant="contained"
                        sx={{ 
                            py: 1.5, 
                            backgroundColor: 'primary.light', 
                            '&:hover': { backgroundColor: 'primary.main' },
                            fontSize: '1.1rem'
                        }}
                    >
                        Log In
                    </Button>
                </Box>
            </Box>
            <Footer />
        </Box>
    );
};
