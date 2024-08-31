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
import { Box, Typography, Theme } from "@oxygen-ui/react";

export const Footer = () => {
    const currentYear = new Date().getFullYear();

    return (
        <Box
            component="footer"
            sx={{
                backgroundColor: '#1e202d',
                borderTop: (theme: Theme) => `1px solid ${theme.palette.divider}`,
                color: 'white',
                padding: '8px 16px',
                position: 'fixed',
                bottom: 0,
                left: 0,
                right: 0,
                height: '60px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
            }}
        >
            <Typography 
                variant="body1" 
                align="center" 
            >
                Guardio Life © {currentYear} | Powered by WSO2 Identity Server
            </Typography>
        </Box>
    );
};
