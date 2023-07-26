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

import React, { FunctionComponent, ReactElement } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Footer, NavBar } from "../components";
import Container from "@mui/material/Container";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";

/**
 * Page to display for 404.
 *
 * @param props - Props injected to the component.
 *
 * @return {React.ReactElement}
 */
export const GenericErrorPage: FunctionComponent = (props): ReactElement => {

    const location = useLocation();
    const navigate = useNavigate();

    return (

        <>
            <NavBar/>
            <Container disableGutters maxWidth="sm" component="main" sx={{ pt: 8, pb: 6, minHeight: "70vh" }}>
                <Typography
                    component="h5"
                    variant="h4"
                    align="center"
                    color="text.primary"
                    gutterBottom
                    sx={{ mb: 3 }}
                >
                    Oops! Something went wrong.
                </Typography>
                <Typography variant="h6" align="center" color="text.secondary" component="p">
                    Error details: {location?.state?.message}
                </Typography>
                <Button variant="contained" sx={{ mt: 3 }} onClick={() => navigate("/")}>
                    Back to home
                </Button>
            </Container>
            <Footer/>
        </>

    );
};
