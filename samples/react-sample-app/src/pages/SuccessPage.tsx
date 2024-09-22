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

import React from 'react';
import { useLocation, useNavigate } from "react-router-dom";
import { Button, Typography, Container, Box } from '@oxygen-ui/react';
import { Footer } from "../components/Footer";
import { NavBar } from "../components/NavBar";

export const SuccessPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const plan = location?.state?.plan;

    return (
        <>
            <NavBar />
            <Container maxWidth="sm" sx={{ py: 8, minHeight: "70vh" }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                    <Typography
                        variant="h4"
                        align="center"
                        color="text.primary"
                        gutterBottom
                    >
                        Thank you for choosing Guardio Life!
                    </Typography>
                    <Typography 
                        variant="h6" 
                        align="center" 
                        color="text.secondary" 
                        sx={{ mt: 2, mb: 4 }}
                    >
                        One of our agents will contact you shortly to walk you through 
                        the next steps of the {plan ?? "selected insurance plan"}.
                    </Typography>
                    <Button 
                        color="primary" 
                        variant="outlined" 
                        onClick={() => navigate("/")}
                        sx={{ mt: 2 }}
                    >
                        Back to home
                    </Button>
                </Box>
            </Container>
            <Footer />
        </>
    );
}
