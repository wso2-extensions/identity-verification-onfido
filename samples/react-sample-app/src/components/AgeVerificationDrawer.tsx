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

import React from 'react';
import { Typography, Button, Box } from "@mui/material";

interface AgeVerificationDrawerProps {
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
    verifyAge: () => void;
    message: string;
    type: string;
    showButton: boolean;
}

export const AgeVerificationDrawer: React.FC<AgeVerificationDrawerProps> = (props) => {
    const {
        isOpen,
        setIsOpen,
        verifyAge,
        message,
        type,
        showButton
    } = props;

    const getBannerBackgroundColorByMessageType = (messageType: string) => {
        switch(messageType){
            case "info":
                return 'gray'
            case "success":
                return "#59ba6f" // light green background for a success message
        }
    }

    return isOpen ? (
            <Box sx={{ 
                p: 2,
                display: 'flex', 
                flexDirection: 'row',
                alignItems: 'center',
                justifyContent: 'center',
                color: "white",
                fontWeight: "bold",
                backgroundColor: getBannerBackgroundColorByMessageType(type)

            }}>
                <Typography variant="body1">
                    {message}
                </Typography>
                {showButton && (
                    <Button
                        variant="contained"
                        onClick={verifyAge}
                        sx={{
                            ml: 2
                        }}
                    >
                        Verify Age
                    </Button>
                )}
            </Box>
    ) : null;
};
