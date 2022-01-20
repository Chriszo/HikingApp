package com.example.hikingapp.utils

class SampleData {

    companion object {

        const val rawWeatherData = "{\n" +
                "\t\"latitude\": 37.97462,\n" +
                "\t\"longitude\": 23.72004,\n" +
                "\t\"timezone\": \"Europe/Athens\",\n" +
                "\t\"currently\": {\n" +
                "\t\t\"time\": 1642664798,\n" +
                "\t\t\"summary\": \"Clear\",\n" +
                "\t\t\"icon\": \"clear-day\",\n" +
                "\t\t\"precipIntensity\": 0,\n" +
                "\t\t\"precipProbability\": 0,\n" +
                "\t\t\"temperature\": 5.61,\n" +
                "\t\t\"apparentTemperature\": 4.47,\n" +
                "\t\t\"dewPoint\": 0.96,\n" +
                "\t\t\"humidity\": 0.72,\n" +
                "\t\t\"pressure\": 1027.5,\n" +
                "\t\t\"windSpeed\": 1.64,\n" +
                "\t\t\"windGust\": 2.49,\n" +
                "\t\t\"windBearing\": 219,\n" +
                "\t\t\"cloudCover\": 0.02,\n" +
                "\t\t\"uvIndex\": 1,\n" +
                "\t\t\"visibility\": 16.093,\n" +
                "\t\t\"ozone\": 328.5\n" +
                "\t},\n" +
                "\t\"daily\": {\n" +
                "\t\t\"summary\": \"Rain on Saturday through next Thursday.\",\n" +
                "\t\t\"icon\": \"rain\",\n" +
                "\t\t\"data\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1642629600,\n" +
                "\t\t\t\t\"summary\": \"Clear throughout the day.\",\n" +
                "\t\t\t\t\"icon\": \"clear-day\",\n" +
                "\t\t\t\t\"sunriseTime\": 1642657140,\n" +
                "\t\t\t\t\"sunsetTime\": 1642692960,\n" +
                "\t\t\t\t\"moonPhase\": 0.59,\n" +
                "\t\t\t\t\"precipIntensity\": 0,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0,\n" +
                "\t\t\t\t\"precipProbability\": 0,\n" +
                "\t\t\t\t\"temperatureHigh\": 13.19,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1642684380,\n" +
                "\t\t\t\t\"temperatureLow\": 5.75,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1642732080,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 12.91,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1642684380,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": 3.8,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1642734000,\n" +
                "\t\t\t\t\"dewPoint\": 1.61,\n" +
                "\t\t\t\t\"humidity\": 0.7,\n" +
                "\t\t\t\t\"pressure\": 1025.6,\n" +
                "\t\t\t\t\"windSpeed\": 1.68,\n" +
                "\t\t\t\t\"windGust\": 3.51,\n" +
                "\t\t\t\t\"windGustTime\": 1642675500,\n" +
                "\t\t\t\t\"windBearing\": 241,\n" +
                "\t\t\t\t\"cloudCover\": 0.04,\n" +
                "\t\t\t\t\"uvIndex\": 3,\n" +
                "\t\t\t\t\"uvIndexTime\": 1642675200,\n" +
                "\t\t\t\t\"visibility\": 16.093,\n" +
                "\t\t\t\t\"ozone\": 328.2,\n" +
                "\t\t\t\t\"temperatureMin\": 2.29,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1642656300,\n" +
                "\t\t\t\t\"temperatureMax\": 13.19,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1642684380,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": 1.24,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1642654680,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 12.91,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1642684380\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1642716000,\n" +
                "\t\t\t\t\"summary\": \"Partly cloudy throughout the day.\",\n" +
                "\t\t\t\t\"icon\": \"partly-cloudy-day\",\n" +
                "\t\t\t\t\"sunriseTime\": 1642743480,\n" +
                "\t\t\t\t\"sunsetTime\": 1642779420,\n" +
                "\t\t\t\t\"moonPhase\": 0.62,\n" +
                "\t\t\t\t\"precipIntensity\": 0.0005,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0.0021,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1642756920,\n" +
                "\t\t\t\t\"precipProbability\": 0.05,\n" +
                "\t\t\t\t\"precipType\": \"rain\",\n" +
                "\t\t\t\t\"temperatureHigh\": 16.17,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1642770180,\n" +
                "\t\t\t\t\"temperatureLow\": 6.49,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1642827600,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 15.89,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1642770180,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": 3.82,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1642827600,\n" +
                "\t\t\t\t\"dewPoint\": 4.75,\n" +
                "\t\t\t\t\"humidity\": 0.7,\n" +
                "\t\t\t\t\"pressure\": 1017.9,\n" +
                "\t\t\t\t\"windSpeed\": 3.32,\n" +
                "\t\t\t\t\"windGust\": 9.39,\n" +
                "\t\t\t\t\"windGustTime\": 1642761540,\n" +
                "\t\t\t\t\"windBearing\": 243,\n" +
                "\t\t\t\t\"cloudCover\": 0.44,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1642761600,\n" +
                "\t\t\t\t\"visibility\": 16.093,\n" +
                "\t\t\t\t\"ozone\": 328.2,\n" +
                "\t\t\t\t\"temperatureMin\": 5.75,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1642732080,\n" +
                "\t\t\t\t\"temperatureMax\": 16.17,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1642770180,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": 3.8,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1642734000,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 15.89,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1642770180\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1642802400,\n" +
                "\t\t\t\t\"summary\": \"Rain in the morning and afternoon.\",\n" +
                "\t\t\t\t\"icon\": \"rain\",\n" +
                "\t\t\t\t\"sunriseTime\": 1642829880,\n" +
                "\t\t\t\t\"sunsetTime\": 1642865880,\n" +
                "\t\t\t\t\"moonPhase\": 0.65,\n" +
                "\t\t\t\t\"precipIntensity\": 0.2436,\n" +
                "\t\t\t\t\"precipIntensityMax\": 1.0647,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1642834140,\n" +
                "\t\t\t\t\"precipProbability\": 0.81,\n" +
                "\t\t\t\t\"precipType\": \"rain\",\n" +
                "\t\t\t\t\"temperatureHigh\": 7.04,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1642827600,\n" +
                "\t\t\t\t\"temperatureLow\": 1.72,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1642902360,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 3.82,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1642827600,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": -3.94,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1642902240,\n" +
                "\t\t\t\t\"dewPoint\": -0.35,\n" +
                "\t\t\t\t\"humidity\": 0.66,\n" +
                "\t\t\t\t\"pressure\": 1016.2,\n" +
                "\t\t\t\t\"windSpeed\": 5.32,\n" +
                "\t\t\t\t\"windGust\": 13.46,\n" +
                "\t\t\t\t\"windGustTime\": 1642888800,\n" +
                "\t\t\t\t\"windBearing\": 351,\n" +
                "\t\t\t\t\"cloudCover\": 0.72,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1642847940,\n" +
                "\t\t\t\t\"visibility\": 13.338,\n" +
                "\t\t\t\t\"ozone\": 353.8,\n" +
                "\t\t\t\t\"temperatureMin\": 2.31,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1642888800,\n" +
                "\t\t\t\t\"temperatureMax\": 9.24,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1642802400,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": -2.94,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1642888800,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 7.51,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1642802400\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1642888800,\n" +
                "\t\t\t\t\"summary\": \"Partly cloudy throughout the day.\",\n" +
                "\t\t\t\t\"icon\": \"partly-cloudy-day\",\n" +
                "\t\t\t\t\"sunriseTime\": 1642916220,\n" +
                "\t\t\t\t\"sunsetTime\": 1642952340,\n" +
                "\t\t\t\t\"moonPhase\": 0.69,\n" +
                "\t\t\t\t\"precipIntensity\": 0.0642,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0.2967,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1642921440,\n" +
                "\t\t\t\t\"precipProbability\": 0.37,\n" +
                "\t\t\t\t\"precipType\": \"rain\",\n" +
                "\t\t\t\t\"temperatureHigh\": 5.58,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1642935900,\n" +
                "\t\t\t\t\"temperatureLow\": -0.13,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1642978260,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 0.38,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1642937340,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": -3.75,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1642968360,\n" +
                "\t\t\t\t\"dewPoint\": -7.27,\n" +
                "\t\t\t\t\"humidity\": 0.49,\n" +
                "\t\t\t\t\"pressure\": 1021,\n" +
                "\t\t\t\t\"windSpeed\": 7.15,\n" +
                "\t\t\t\t\"windGust\": 15.04,\n" +
                "\t\t\t\t\"windGustTime\": 1642901880,\n" +
                "\t\t\t\t\"windBearing\": 348,\n" +
                "\t\t\t\t\"cloudCover\": 0.36,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1642934040,\n" +
                "\t\t\t\t\"visibility\": 16.093,\n" +
                "\t\t\t\t\"ozone\": 384.3,\n" +
                "\t\t\t\t\"temperatureMin\": -0.02,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1642975200,\n" +
                "\t\t\t\t\"temperatureMax\": 5.58,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1642935900,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": -3.94,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1642902240,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 0.38,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1642937340\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1642975200,\n" +
                "\t\t\t\t\"summary\": \"Mostly cloudy throughout the day.\",\n" +
                "\t\t\t\t\"icon\": \"rain\",\n" +
                "\t\t\t\t\"sunriseTime\": 1643002620,\n" +
                "\t\t\t\t\"sunsetTime\": 1643038800,\n" +
                "\t\t\t\t\"moonPhase\": 0.72,\n" +
                "\t\t\t\t\"precipIntensity\": 0.0877,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0.2968,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1643057880,\n" +
                "\t\t\t\t\"precipProbability\": 0.45,\n" +
                "\t\t\t\t\"precipType\": \"rain\",\n" +
                "\t\t\t\t\"temperatureHigh\": 5.37,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1643015100,\n" +
                "\t\t\t\t\"temperatureLow\": 1.1,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1643074200,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 1.82,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1643014680,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": -4.17,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1643074740,\n" +
                "\t\t\t\t\"dewPoint\": -6.46,\n" +
                "\t\t\t\t\"humidity\": 0.53,\n" +
                "\t\t\t\t\"pressure\": 1022.2,\n" +
                "\t\t\t\t\"windSpeed\": 5.36,\n" +
                "\t\t\t\t\"windGust\": 15.98,\n" +
                "\t\t\t\t\"windGustTime\": 1643047440,\n" +
                "\t\t\t\t\"windBearing\": 357,\n" +
                "\t\t\t\t\"cloudCover\": 0.61,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1643020500,\n" +
                "\t\t\t\t\"visibility\": 15.527,\n" +
                "\t\t\t\t\"ozone\": 396.2,\n" +
                "\t\t\t\t\"temperatureMin\": -0.13,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1642978260,\n" +
                "\t\t\t\t\"temperatureMax\": 5.37,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1643015100,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": -4.03,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1643048160,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 1.82,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1643014680\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1643061600,\n" +
                "\t\t\t\t\"summary\": \"Foggy in the morning.\",\n" +
                "\t\t\t\t\"icon\": \"rain\",\n" +
                "\t\t\t\t\"sunriseTime\": 1643088960,\n" +
                "\t\t\t\t\"sunsetTime\": 1643125260,\n" +
                "\t\t\t\t\"moonPhase\": 0.76,\n" +
                "\t\t\t\t\"precipIntensity\": 0.1046,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0.3457,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1643082360,\n" +
                "\t\t\t\t\"precipProbability\": 0.42,\n" +
                "\t\t\t\t\"precipType\": \"rain\",\n" +
                "\t\t\t\t\"temperatureHigh\": 4.53,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1643113440,\n" +
                "\t\t\t\t\"temperatureLow\": -1.58,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1643156940,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": -0.38,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1643115240,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": -4.15,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1643134020,\n" +
                "\t\t\t\t\"dewPoint\": -7.05,\n" +
                "\t\t\t\t\"humidity\": 0.54,\n" +
                "\t\t\t\t\"pressure\": 1026.2,\n" +
                "\t\t\t\t\"windSpeed\": 5.2,\n" +
                "\t\t\t\t\"windGust\": 13.77,\n" +
                "\t\t\t\t\"windGustTime\": 1643076840,\n" +
                "\t\t\t\t\"windBearing\": 351,\n" +
                "\t\t\t\t\"cloudCover\": 0.48,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1643107320,\n" +
                "\t\t\t\t\"visibility\": 12.305,\n" +
                "\t\t\t\t\"ozone\": 396.6,\n" +
                "\t\t\t\t\"temperatureMin\": -1.24,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1643140380,\n" +
                "\t\t\t\t\"temperatureMax\": 4.53,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1643113440,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": -4.17,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1643074740,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": -0.38,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1643115240\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1643148000,\n" +
                "\t\t\t\t\"summary\": \"Mostly cloudy throughout the day.\",\n" +
                "\t\t\t\t\"icon\": \"partly-cloudy-day\",\n" +
                "\t\t\t\t\"sunriseTime\": 1643175300,\n" +
                "\t\t\t\t\"sunsetTime\": 1643211720,\n" +
                "\t\t\t\t\"moonPhase\": 0.79,\n" +
                "\t\t\t\t\"precipIntensity\": 0,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1643234400,\n" +
                "\t\t\t\t\"precipProbability\": 0,\n" +
                "\t\t\t\t\"temperatureHigh\": 10.48,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1643199840,\n" +
                "\t\t\t\t\"temperatureLow\": 2.58,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1643247300,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 10.34,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1643200320,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": -0.36,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1643247480,\n" +
                "\t\t\t\t\"dewPoint\": -6.63,\n" +
                "\t\t\t\t\"humidity\": 0.49,\n" +
                "\t\t\t\t\"pressure\": 1024.5,\n" +
                "\t\t\t\t\"windSpeed\": 2.24,\n" +
                "\t\t\t\t\"windGust\": 11.18,\n" +
                "\t\t\t\t\"windGustTime\": 1643215020,\n" +
                "\t\t\t\t\"windBearing\": 349,\n" +
                "\t\t\t\t\"cloudCover\": 0.43,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1643193600,\n" +
                "\t\t\t\t\"visibility\": 16.093,\n" +
                "\t\t\t\t\"ozone\": 392.5,\n" +
                "\t\t\t\t\"temperatureMin\": -1.58,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1643156940,\n" +
                "\t\t\t\t\"temperatureMax\": 10.48,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1643199840,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": -3.12,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1643154120,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 10.34,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1643200320\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"time\": 1643234400,\n" +
                "\t\t\t\t\"summary\": \"Possible light rain in the evening and overnight.\",\n" +
                "\t\t\t\t\"icon\": \"rain\",\n" +
                "\t\t\t\t\"sunriseTime\": 1643261700,\n" +
                "\t\t\t\t\"sunsetTime\": 1643298180,\n" +
                "\t\t\t\t\"moonPhase\": 0.83,\n" +
                "\t\t\t\t\"precipIntensity\": 0.0802,\n" +
                "\t\t\t\t\"precipIntensityMax\": 0.4766,\n" +
                "\t\t\t\t\"precipIntensityMaxTime\": 1643320800,\n" +
                "\t\t\t\t\"precipProbability\": 0.52,\n" +
                "\t\t\t\t\"precipType\": \"rain\",\n" +
                "\t\t\t\t\"temperatureHigh\": 9.63,\n" +
                "\t\t\t\t\"temperatureHighTime\": 1643286060,\n" +
                "\t\t\t\t\"temperatureLow\": 3.35,\n" +
                "\t\t\t\t\"temperatureLowTime\": 1643328060,\n" +
                "\t\t\t\t\"apparentTemperatureHigh\": 8.22,\n" +
                "\t\t\t\t\"apparentTemperatureHighTime\": 1643285580,\n" +
                "\t\t\t\t\"apparentTemperatureLow\": 0.57,\n" +
                "\t\t\t\t\"apparentTemperatureLowTime\": 1643328360,\n" +
                "\t\t\t\t\"dewPoint\": -5.04,\n" +
                "\t\t\t\t\"humidity\": 0.49,\n" +
                "\t\t\t\t\"pressure\": 1025,\n" +
                "\t\t\t\t\"windSpeed\": 2.96,\n" +
                "\t\t\t\t\"windGust\": 6.8,\n" +
                "\t\t\t\t\"windGustTime\": 1643260860,\n" +
                "\t\t\t\t\"windBearing\": 22,\n" +
                "\t\t\t\t\"cloudCover\": 0.56,\n" +
                "\t\t\t\t\"uvIndex\": 2,\n" +
                "\t\t\t\t\"uvIndexTime\": 1643279520,\n" +
                "\t\t\t\t\"visibility\": 16.093,\n" +
                "\t\t\t\t\"ozone\": 381.5,\n" +
                "\t\t\t\t\"temperatureMin\": 2.57,\n" +
                "\t\t\t\t\"temperatureMinTime\": 1643261460,\n" +
                "\t\t\t\t\"temperatureMax\": 9.63,\n" +
                "\t\t\t\t\"temperatureMaxTime\": 1643286060,\n" +
                "\t\t\t\t\"apparentTemperatureMin\": -0.36,\n" +
                "\t\t\t\t\"apparentTemperatureMinTime\": 1643247480,\n" +
                "\t\t\t\t\"apparentTemperatureMax\": 8.22,\n" +
                "\t\t\t\t\"apparentTemperatureMaxTime\": 1643285580\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t},\n" +
                "\t\"alerts\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"title\": \"Yellow Warning For Attiki\",\n" +
                "\t\t\t\"regions\": [\n" +
                "\t\t\t\t\"ATTIKI\"\n" +
                "\t\t\t],\n" +
                "\t\t\t\"severity\": \"warning\",\n" +
                "\t\t\t\"time\": 1642528800,\n" +
                "\t\t\t\"expires\": 1642665600,\n" +
                "\t\t\t\"description\": \"Locally low temperatures / ice in the evening and in the morning.\\n\",\n" +
                "\t\t\t\"uri\": \"http://www.hnms.gr\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"flags\": {\n" +
                "\t\t\"sources\": [\n" +
                "\t\t\t\"meteoalarm\",\n" +
                "\t\t\t\"cmc\",\n" +
                "\t\t\t\"gfs\",\n" +
                "\t\t\t\"icon\",\n" +
                "\t\t\t\"isd\",\n" +
                "\t\t\t\"madis\"\n" +
                "\t\t],\n" +
                "\t\t\"meteoalarm-license\": \"Based on data from EUMETNET - MeteoAlarm [https://www.meteoalarm.eu/]. Time delays between this website and the MeteoAlarm website are possible; for the most up to date information about alert levels as published by the participating National Meteorological Services please use the MeteoAlarm website.\",\n" +
                "\t\t\"nearest-station\": 10.364,\n" +
                "\t\t\"units\": \"si\"\n" +
                "\t},\n" +
                "\t\"offset\": 2\n" +
                "}"
    }

}