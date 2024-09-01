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
            <Box sx={{ 
                position: 'absolute', 
                top: 0,
                right: 0,
                zIndex: 1000,
                marginTop: '16px',
                marginRight: '16px'
            }}>
                <ButtonGroup 
                    variant="outlined" 
                    sx={{ 
                        borderRadius: '12px',
                        overflow: 'visible',
                        backgroundColor: 'white',
                        border: '1px solid #f0f0f0',
                        '& .MuiButtonGroup-grouped:not(:last-of-type)': {
                            borderColor: '#f0f0f0',
                            borderRightWidth: '1px',
                        },
                        boxShadow: '0 0 0 1px #f0f0f0',
                    }}
                >
                    <MuiButton
                        startIcon={<EmailIcon />}
                        sx={{ 
                            color: 'black', 
                            borderRadius: '12px 0 0 12px',
                            position: 'relative',
                            paddingRight: '16px',
                            paddingLeft: '16px',
                            '&:hover': { backgroundColor: 'transparent' },
                            borderColor: '#f0f0f0',
                            borderWidth: '1px',
                            fontSize: '0.875rem',
                        }}
                    >
                        Messages
                        <Box component="span" sx={{
                            position: 'absolute',
                            top: -12,
                            right: -15,
                            backgroundColor: 'error.main',
                            color: 'white',
                            borderRadius: '12px',
                            padding: '0 6px',
                            fontSize: '0.75rem',
                            minWidth: '33px',
                            height: '20px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            zIndex: 1,
                        }}>
                            22
                        </Box>
                    </MuiButton>
                    <MuiButton
                        startIcon={<GroupIcon />}
                        sx={{ 
                            color: 'black',
                            borderRadius: '0 12px 12px 0',
                            position: 'relative',
                            paddingRight: '16px',
                            paddingLeft: '16px',
                            '&:hover': { backgroundColor: 'transparent' },
                            borderColor: '#f0f0f0',
                            borderWidth: '1px',
                            fontSize: '0.875rem',
                        }}
                    >
                        Notifications
                        <Box component="span" sx={{
                            position: 'absolute',
                            top: -12,
                            right: -15,
                            backgroundColor: '#00C6AE', 
                            color: 'white',
                            borderRadius: '12px',
                            padding: '0 6px',
                            fontSize: '0.75rem',
                            minWidth: '33px',
                            height: '20px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            zIndex: 1,
                        }}>
                            22
                        </Box>
                    </MuiButton>
                </ButtonGroup>
            </Box>
            
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
