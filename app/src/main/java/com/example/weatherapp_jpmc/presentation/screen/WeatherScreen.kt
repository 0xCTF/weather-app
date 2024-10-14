package com.example.weatherapp_jpmc.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.weatherapp_jpmc.domain.model.WeatherUiState
import com.example.weatherapp_jpmc.presentation.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, viewModel: WeatherViewModel = hiltViewModel()) {
    var city by remember { mutableStateOf("") } // State for the city input
    val uiState by viewModel.uiState.observeAsState() // Observe UI state from ViewModel

    // Search bar visibility state
    var searchVisible by remember { mutableStateOf(false) }

    // Get the focus manager to dismiss the keyboard
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C2833)) // Dark navy background
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row for Search Icon and Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Search bar and icon toggle
            AnimatedVisibility(visible = searchVisible) {
                BasicTextField(
                    value = city,
                    onValueChange = {
                        if (it.length <= 20) city = it // Limiting text to 20 characters
                    },
                    maxLines = 1, // Limit to one line
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search // Shows search icon on the keyboard
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.getWeather(city) // Trigger the search
                            searchVisible = false // Hide search bar and button after search
                            focusManager.clearFocus() // Dismiss the keyboard
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(8.dp)
                )
            }

            IconButton(onClick = {
                if (searchVisible && city.isNotEmpty()) {
                    viewModel.getWeather(city) // Trigger search on icon click
                    searchVisible = false // Hide search bar and button
                    focusManager.clearFocus() // Dismiss the keyboard
                } else {
                    searchVisible = !searchVisible // Toggle visibility
                }
            }) {
                Icon(
                    imageVector = if (searchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Weather Information
        if (uiState is WeatherUiState.Success) {
            val weather = (uiState as WeatherUiState.Success).data
            val description = weather.weather[0].description
            val cityName = weather.name
            val countryName = weather.sys.country
            val wind = weather.wind.speed
            val pressure = weather.main.pressure
            val humidity = weather.main.humidity

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2C3E50)) // Darker navy for the main card
                    .padding(24.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Weather icon
                AsyncImage(
                    model = "https://openweathermap.org/img/w/${weather.weather[0].icon}.png",
                    modifier = Modifier.size(100.dp),
                    contentDescription = "Weather icon"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bold Temperature Text
                TemperatureText(temperature = weather.main.temp.toInt())

                Spacer(modifier = Modifier.height(8.dp))

                // City and country Name
                Text(
                    text = "$cityName, $countryName",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weather Description
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Three stat cards aligned vertically
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Space between the cards
                ) {
                    WeatherStatCard(
                        label = "Wind Flow",
                        value = "$wind mph",
                        iconUrl = "https://cdn-icons-png.flaticon.com/512/2057/2057945.png"
                    )
                    WeatherStatCard(
                        label = "Pressure",
                        value = "$pressure mb",
                        iconUrl = "https://cdn-icons-png.flaticon.com/512/10441/10441050.png"
                    )
                    WeatherStatCard(
                        label = "Humidity",
                        value = "$humidity%",
                        iconUrl = "https://cdn-icons-png.flaticon.com/512/5664/5664979.png"
                    )
                }
            }
        }
    else if (uiState is WeatherUiState.Error) {
        // Display an image when there is an error
        AsyncImage(
            model = "https://s.w-x.co/util/image/w/earthrise.jpg?v=at&w=1440&h=2560",
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Error Image"
        )
        }
    }
}

@Composable
fun WeatherStatCard(label: String, value: String, iconUrl: String) {
    Row(
        modifier = Modifier
            .size(width = 300.dp, height = 100.dp) // Fixed size for the card
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF34495E)) // Light navy color
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, // Center the icon and text vertically
        horizontalArrangement = Arrangement.SpaceBetween // Spread the text and icon horizontally
    ) {
        // Text on the left
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Value Text
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Label Text
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                color = Color.White
            )
        }

        // Icon on the right
        AsyncImage(
            model = iconUrl,
            modifier = Modifier.size(48.dp), // Set icon size
            contentDescription = "$label icon"
        )
    }
}

@Composable
fun TemperatureText(temperature: Int) {
    Text(
        text = buildAnnotatedString {
            append("$temperature") // Temperature number

            // Adding the °F part with smaller text
            withStyle(
                style = SpanStyle(
                    fontSize = 16.sp, // Smaller font size
                    baselineShift = BaselineShift.Superscript, // Align it to the top
                    fontWeight = FontWeight.Light // Optional, make the °F a bit lighter in weight
                )
            ) {
                append("°F")
            }
        },
        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.White
    )
}
