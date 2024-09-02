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
import React, { FunctionComponent, ReactElement, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Footer, LoadingSpinner, NavBar } from "../components";
import { Typography, Button, Box, TextField, Checkbox, FormControlLabel } from "@oxygen-ui/react";

const GUARDIO_FAMILY_IMAGE = `${process.env.REACT_APP_BASE_URL}/images/guardio-family.svg`;

export const LoginPage: FunctionComponent = (): ReactElement => {
    const { state } = useAuthContext();
    const navigate = useNavigate();

    useEffect(() => {
        if (state?.isAuthenticated) {
            navigate("/");
        }
    }, [state?.isAuthenticated, navigate]);

    if (state?.isLoading) {
        return <LoadingSpinner />;
    }

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
                        The Right Protection
                    </Typography>
                    <Typography variant="h5" component="h2" gutterBottom color="text.secondary" sx={{ mb: 3 }}>
                        for You and Your Family
                    </Typography>
                    <Typography variant="body1" paragraph sx={{ mb: 4, fontSize: '1.1rem' }}>
                        Excellence in life insurance since 2001. We are one of the leading life insurers in the world. Choose your life with the most loved health insurance company.
                    </Typography>
                    <Box component="form" noValidate sx={{ width: '100%' }}>
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            id="email"
                            placeholder="Your email"
                            name="email"
                            autoComplete="email"
                            autoFocus
                            InputProps={{
                                sx: { 
                                    fontSize: '1.1rem',
                                    '&::placeholder': {
                                        opacity: 1
                                    }
                                }
                            }}
                            sx={{ mb: 1 }}
                        />
                        <Typography variant="body2" sx={{ display: 'block', mb: 2, fontSize: '0.9rem' }}>
                            Please enter your email in the above field if you wish to download our policy. Don&apos;t worry, we won&apos;t spam you.
                        </Typography>
                        <FormControlLabel
                            control={<Checkbox value="subscribe" color="primary" />}
                            label={
                                <Typography sx={{ fontSize: '1rem' }}>
                                    Subscribe me to the Guardio newsletter
                                </Typography>
                            }
                            sx={{ mb: 2 }}
                        />
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            sx={{ 
                                py: 1.5, 
                                backgroundColor: 'primary.light', 
                                '&:hover': { backgroundColor: 'primary.main' },
                                fontSize: '1.1rem'
                            }}
                        >
                            Download Policy
                        </Button>
                    </Box>
                </Box>
            </Box>
            <Footer />
        </Box>
    );
};