# WeatherFlow — Google Play Store Production & ASO Kit

**Package Name:** `com.osquareapps.weather`  
**Developer Name:** `O Square Apps`  
**Official Developer Website:** `https://weatherflow-3f663.web.app/`  
**Privacy Policy URL:** `https://weatherflow-3f663.web.app/privacy.html`  
**How to Use Guide:** `https://weatherflow-3f663.web.app/how-to-use.html`  
**app-ads.txt URL:** `https://weatherflow-3f663.web.app/app-ads.txt`  
**AdMob Publisher ID:** `pub-7787849898368360`

---

## 1. Google Play Store Listing & ASO Metadata

### App Title (Max 30 characters)
```text
WeatherFlow: Live Weather & AQI
```

### Short Description (Max 80 characters)
```text
Live accurate weather radar, rain planner, MET Norway forecast & air quality.
```

### Full Description (Up to 4,000 characters)
```text
Experience precision atmospheric intelligence with WeatherFlow — the meteorologist-grade weather app built on scientific accuracy, transparent data models, and zero guesswork.

Powered directly by MET Norway (Meteorologisk Institutt) and European Copernicus CAMS air quality systems, WeatherFlow gives you honest, localized weather predictions without synthetic estimates or deceptive minute-by-minute countdowns.

🌦️ KEY FEATURES:

1. CERTIFIED MET NORWAY FORECASTS
• Authentic Locationforecast 2.0 data models under Creative Commons BY 4.0.
• Hyper-local hourly temperatures, precipitation probabilities, dew point, cloud cover, and wind velocity.
• Multi-day temperature range predictions with provider model run timestamps.

2. REAL CHEMICAL AIR QUALITY (US AQI & CAMS)
• Live microgram pollutant measurements: Fine Particles (PM2.5), Coarse Particles (PM10), Ground Ozone (O₃), and Nitrogen Dioxide (NO₂).
• Real EPA-standard US AQI index (0 to 500) with clear health category breakdowns.
• Outdoor cardio and exercise safety ratings tailored to sensitive groups and athletic routines.

3. SCIENTIFIC RAIN TIMING & CONVECTIVE UNCERTAINTY
• Atmospheric convective precipitation cells fluctuate across kilometers. WeatherFlow provides realistic clustered rain prediction windows (e.g. 14:00 – 17:00) with peak intensity in mm/hr rather than guessing exact minutes.
• Microburst moisture divergence warnings.

4. 5 MATERIAL YOU FROSTED GLASS WIDGETS
• 1x1 Quick Glance: Instant temperature badge and condition icon.
• 4x1 Modern Pill: Sleek horizontal bar with high/low and precipitation rate.
• 2x2 Radial Dial: Circular gauge with thermal envelope, wind speed, and humidity.
• 4x2 Forecast Matrix: Complete multi-day outlook with hourly projections.
• 3x3 Atmospheric Radar: Radar glance, air quality meter, and astronomical sun path.
• Interactive Widgets Studio: Configure background glass opacity (0–100%) and pure OLED dark mode.

5. ASTRONOMICAL SOLAR & LUNAR TRACKER
• Accurate Sunrise, Solar Noon, and Sunset calculations based on NOAA solar geometry.
• Dynamic moon phase tracking: Waxing/Waning crescent, illumination percentage, and lunar cycle days.

6. BAROMETER & PRESSURE TREND ANALYSIS
• Real-time barometric pressure in hPa with 3-hour delta analysis.
• Rapid pressure drop indicators for approaching squalls and frontal systems.

7. ACTIVITY PLANNER & PERSONAL THERMAL ENVELOPE
• Set your personal comfort temperature range (e.g. 16°C – 24°C).
• Real-time suitability scores (0–100%) for Running, Cycling, Walking, and Outdoor Workouts.

🔒 PRIVACY & DATA SAFETY:
• Coordinates rounded to 4 decimals (~11 meters) on-device before querying weather APIs. We never record or track your exact residential address.
• Zero sale or brokering of user data to third-party data aggregators.
• Full compliance with Google Play Data Safety policies and GDPR standards.

Download WeatherFlow today for honest, beautiful, and transparent weather intelligence.
```

### Categorization & Tags
- **Application Category:** Weather
- **Content Rating:** Everyone (PEGI 3 / ESRB Everyone)
- **High-Intent Search Tags:** weather radar, air quality index, MET Norway, rain planner, live weather widgets, AQI PM2.5, barometer pressure tracker, offline weather forecast.

---

## 2. Google Play Console Data Safety Form Guide

| Data Category | Collected? | Shared? | Purpose | Retention / Deletion |
| :--- | :--- | :--- | :--- | :--- |
| **Location (Approximate & Precise)** | Yes | No (Processed Ephemerally) | App functionality, Personalization, Widget forecast updates | Coordinates are rounded to 4 decimals (~11m); not stored permanently on remote servers |
| **App Info and Performance (Crash logs, diagnostics)** | Yes | No | Analytics & Crash Reporting via Google Firebase | Retained per Firebase standard retention policies |
| **Device or other IDs** | Yes | No | Firebase Installation ID for analytics & notifications | Ephemeral; reset upon app uninstall |
| **Personal Info (Name, Email, Phone)** | **No** | **No** | N/A | None collected |
| **Financial / Payment Info** | **No** | **No** | N/A | None collected |

- **Security Practices:**
  - Data is encrypted in transit over HTTPS / TLS.
  - Users can request deletion of data by clearing app storage in Android Settings.

---

## 3. AdMob app-ads.txt Setup & Verification

1. **File Location:**  
   The `app-ads.txt` file is located at the root of your developer web domain:  
   `https://weatherflow-3f663.web.app/app-ads.txt`
2. **Exact Content:**  
   ```text
   google.com, pub-7787849898368360, DIRECT, f08c47fec0942fa0
   ```
3. **Verification Steps:**  
   - In Google Play Console &gt; Store presence &gt; Store settings, set your **Website** to:  
     `https://weatherflow-3f663.web.app/`
   - In Google AdMob &gt; Apps &gt; View all apps &gt; **app-ads.txt**, confirm status. AdMob automatically crawls your website's `/app-ads.txt` within 24 hours.

---

## 4. Deploying Web Landing Page to Firebase Hosting

To publish your developer website, Privacy Policy, How-to-Use guide, and `app-ads.txt` to `https://weatherflow-3f663.web.app`:

```bash
# 1. Login to Firebase (one-time)
npx firebase-tools login

# 2. Deploy hosting from the project root
npx firebase-tools deploy --only hosting
```

All static files are pre-configured in `public/` and ready for instant deployment!
