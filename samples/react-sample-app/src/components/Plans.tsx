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

import { Card, CardContent, Button, Container, Grid, Box, Typography } from '@oxygen-ui/react';
import { ButtonGroup, Button as MuiButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { InsurancePlanCard } from "../model/insurance-plan";
import React from 'react';
import EmailIcon from '@mui/icons-material/Email';
import GroupIcon from '@mui/icons-material/Group';

const plans: InsurancePlanCard[] = [
    {
        title: 'Retirement Plan',
        price: '20',
        description: [
            'Increased ROI to 10%',
            'Extended Hospitalization Cover',
            'Lifetime Life Cover',
            'Enhanced Critical Illness Cover',
        ],
        buttonText: 'Upgrade',
        buttonVariant: 'contained',
    },
    {
        title: 'Advanced Wealth Plan',
        price: '30',
        description: [
            'Boosted ROI to 15%',
            'Premium Hospitalization Cover',
            'Increased Partial Withdrawals',
            'Customizable Term Options',
        ],
        buttonText: 'Upgrade',
        buttonVariant: 'contained',
    },
    {
        title: 'Premium Relief Plan',
        price: '35',
        description: [
            'Maximum ROI of 18%',
            'Comprehensive Hospitalization Cover',
            'Extended Family Health Cover',
            'Priority Claims Processing',
        ],
        buttonText: 'Upgrade',
        buttonVariant: 'contained',
    },
];

interface PlanProps {
    isAgeVerified: boolean;
    setIsDrawerOpen: (isDrawerOpen: boolean) => void;
}

export const Plans = (props: PlanProps) => {
    const { isAgeVerified, setIsDrawerOpen } = props;
    const navigate = useNavigate();

    /**
     * If the user is age verified, navigate to the success page. Else, open the age verification drawer.
     *
     * @param plan - The selected insurance plan.
     */
    const handlePlanSelection = (plan: InsurancePlanCard) => {
        if (isAgeVerified) {
            navigate("/success", { state: { plan: plan.title } });
        } else {
            setIsDrawerOpen(true);
        }
    }

    return (
        <Container maxWidth="lg" sx={{ py: 8, position: 'relative' }}>
            <Typography variant="h2" align="center" gutterBottom>
                Upgrade Your Subscription
            </Typography>
            <Typography align="center" sx={{ mt: 4, mb: 6 }}>
                Enhance your Guardio Life plan to better suit your evolving needs. 
                Enjoy improved benefits, higher coverage, and exclusive features with our premium upgrades.
            </Typography>
            <Grid container spacing={4} justifyContent="center">
                {plans.map((plan: InsurancePlanCard) => (
                    <Grid key={plan.title} xs={12} sm={6} md={4}>
                        <Card
                            elevation={2}
                            sx={{
                                height: '100%',
                                display: 'flex',
                                flexDirection: 'column',
                                transition: 'transform 0.3s ease-in-out',
                                '&:hover': {
                                    transform: 'translateY(-8px)',
                                },
                            }}
                        >
                            <CardContent sx={{ flexGrow: 1 }}>
                                <Typography variant="h3" align="center" gutterBottom>
                                    {plan.title}
                                </Typography>
                                <Typography variant="h4" align="center" sx={{ mb: 2 }}>
                                    ${plan.price}<Typography component="span" variant="body2">/mo</Typography>
                                </Typography>
                                {plan.description.map((line) => (
                                    <Typography key={line} align="center" sx={{ mb: 1 }}>
                                        {line}
                                    </Typography>
                                ))}
                            </CardContent>
                            <CardContent>
                                <Button
                                    fullWidth
                                    variant="contained"
                                    disabled={!isAgeVerified}
                                    onClick={() => handlePlanSelection(plan)}
                                >
                                    {plan.buttonText}
                                </Button>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Container>
    );
}
