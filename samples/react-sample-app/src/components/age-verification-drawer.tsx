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

import React from 'react';
import { Drawer, Typography, Button, Box } from "@mui/material";

interface AgeVerificationDrawerProps {
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
    verifyAge: () => void;
    message: string;
    showButton: boolean;
}

export const AgeVerificationDrawer: React.FC<AgeVerificationDrawerProps> = ({
    isOpen,
    setIsOpen,
    verifyAge,
    message,
    showButton
}) => {
    return (
        <Drawer
            anchor="bottom"
            open={isOpen}
            onClose={() => setIsOpen(false)}
        >
            <Box sx={{ 
                p: 2, 
                display: 'flex', 
                flexDirection: 'column',
                alignItems: 'center', 
                backgroundColor: '#f5f5f5',
                borderTop: '3px solid #1976d2'
            }}>
                <Typography variant="body1" sx={{ mb: showButton ? 2 : 0 }}>
                    {message}
                </Typography>
                {showButton && (
                    <Button
                        variant="contained"
                        onClick={verifyAge}
                        sx={{ mt: 1 }}
                    >
                        Verify Age
                    </Button>
                )}
            </Box>
        </Drawer>
    );
};
