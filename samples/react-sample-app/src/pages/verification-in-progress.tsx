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

import * as React from 'react';
import { Button, Typography, Container, Box } from '@oxygen-ui/react';
import { Footer, NavBar } from "../components";
import { useNavigate } from "react-router-dom";

export const VerificationInProgressPage = () => {

    const navigate = useNavigate();

    return (
        <>
            <NavBar/>
            <Container disableGutters maxWidth="sm" component="main" style={{ paddingTop: 64, paddingBottom: 48, minHeight: "70vh" }}>
                <Typography
                    variant="h4"
                    align="center"
                    color="text.primary"
                    gutterBottom
                    style={{ marginBottom: 24 }}
                >
                    Age Verification in Progress!
                </Typography>
                <Typography variant="h6" align="center" color="text.secondary">
                    Once the verification is complete, you can access the Guardio Life Web Portal.
                </Typography>
                <Box display="flex" justifyContent="center" style={{ marginTop: 24 }}>
                    <Button variant="outlined" onClick={() => navigate(`${process.env.REACT_APP_BASE_URL}`)}>
                        Back to home
                    </Button>
                </Box>
            </Container>
            <Footer/>
        </>
    );
}
