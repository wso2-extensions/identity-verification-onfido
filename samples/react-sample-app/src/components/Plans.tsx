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

import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Grid from '@mui/material/Grid';
import StarIcon from '@mui/icons-material/StarBorder';
import Typography from '@mui/material/Typography';
import Container from '@mui/material/Container';
import { useNavigate } from 'react-router-dom';
import { InsurancePlanCard } from "../model/insurance-plan";
import React from 'react';

const plans: InsurancePlanCard[] = [
    {
        title: 'Retirement Plan',
        price: '20',
        description: [
            '8% Minimum ROI',
            'Hospitalization Cover',
            '100 Years Plus - Life Cover',
            'Critical Illness Cover',
        ],
        buttonText: 'Get started',
        buttonVariant: 'contained',
    },
    {
        title: 'Wealth Plan',
        price: '25',
        description: [
            '12% Minimum ROI',
            'Hospitalization Cover',
            'Partial Withdrawals',
            'Flexible Term Options',
        ],
        buttonText: 'Get started',
        buttonVariant: 'contained',
    },
    {
        title: 'Premium Relief Plan',
        price: '30',
        description: [
            '16% Minimum ROI',
            'Hospitalization Cover',
            '100 Years Plus - Life Cover',
            'Family Health Cover',
        ],
        buttonText: 'Get started',
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
        <>
            <Container disableGutters maxWidth="sm" component="main" sx={{ pt: 8, pb: 6 }}>
                <Typography
                    component="h1"
                    variant="h3"
                    align="center"
                    color="text.primary"
                    gutterBottom
                >
                    Choose Your Plan
                </Typography>
                <Typography variant="h6" align="center" color="text.secondary" component="p">
                    Discover the ideal life insurance plan for your future with Guardio Life. Flexible
                    options, competitive premiums, and exceptional service.
                </Typography>
            </Container>
            {/* Insurance Plans Grid */}
            <Container maxWidth="md" component="main">
                <Grid container spacing={5} alignItems="flex-end">
                    {plans.map((plan: InsurancePlanCard) => (
                        <Grid
                            item
                            key={plan.title}
                            xs={12}
                            sm={plan.title === 'Enterprise' ? 12 : 6}
                            md={4}
                        >
                            <Card>
                                <CardHeader
                                    title={plan.title}
                                    titleTypographyProps={{ align: 'center' }}
                                    action={plan.title === 'Pro' ? <StarIcon/> : null}
                                    subheaderTypographyProps={{
                                        align: 'center',
                                    }}
                                    sx={{
                                        backgroundColor: (theme) =>
                                            theme.palette.mode === 'light'
                                                ? theme.palette.grey[200]
                                                : theme.palette.grey[700],
                                    }}
                                />
                                <CardContent>
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'center',
                                            alignItems: 'baseline',
                                            mb: 2,
                                        }}
                                    >
                                        <Typography component="h2" variant="h3" color="text.primary">
                                            ${plan.price}
                                        </Typography>
                                        <Typography variant="h6" color="text.secondary">
                                            /mo
                                        </Typography>
                                    </Box>
                                    <ul>
                                        {plan.description.map((line) => (
                                            <Typography
                                                component="li"
                                                variant="subtitle1"
                                                align="center"
                                                key={line}
                                            >
                                                {line}
                                            </Typography>
                                        ))}
                                    </ul>
                                </CardContent>
                                <CardActions>
                                    <Button
                                        fullWidth
                                        variant={plan.buttonVariant as 'outlined' | 'contained'}
                                        onClick={() => {
                                            handlePlanSelection(plan);
                                        }}
                                    >
                                        {plan.buttonText}
                                    </Button>
                                </CardActions>
                            </Card>
                        </Grid>
                    ))}
                </Grid>
            </Container>
        </>
    );
}
