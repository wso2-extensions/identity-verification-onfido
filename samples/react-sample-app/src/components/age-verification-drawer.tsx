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

import { Drawer, Link, Stack } from "@mui/material";
import * as React from 'react';

interface AgeVerificationDrawerProps {
    isOpen: boolean;
    setIsOpen: (isOpen: boolean) => void;
    verifyAge: () => void;
}

export const AgeVerificationDrawer = (props: AgeVerificationDrawerProps) => {

    const { isOpen, setIsOpen, verifyAge } = props;
    return (
        <Drawer
            anchor="bottom"
            open={isOpen}
            onClose={() => setIsOpen(false)}
        >
            <Stack
                direction="row"
                alignItems="center"
                justifyContent="center"
                spacing={2}
            >
                <h3>
                    You need to verify your age to keep using LifeGuardian Web Portal. Click&nbsp;
                    <Link onClick={verifyAge}>here</Link> to verify age.
                </h3>
            </Stack>
        </Drawer>
    );
};
