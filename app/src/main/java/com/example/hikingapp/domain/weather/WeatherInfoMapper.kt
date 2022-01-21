package com.example.hikingapp.domain.weather

import android.os.Build
import java.util.stream.Collectors

class WeatherInfoMapper {

    companion object {

        fun map(dto: WeatherInfoDto): WeatherInfo
        {
            return WeatherInfo(dto.time, dto.summary, dto.icon, dto.temperatureHigh, dto.temperatureLow)
        }

        fun mapList(dtoList: List<WeatherInfoDto>): List<WeatherInfo> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                dtoList.stream().map { map(it) }.collect(Collectors.toList())
            } else {
                val weatherInfoList = ArrayList<WeatherInfo>()
                dtoList.forEach{
                    weatherInfoList.add(map(it))
                }
                return weatherInfoList
            }
        }
    }

}