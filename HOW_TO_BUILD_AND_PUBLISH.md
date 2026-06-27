# 🏙️ City Runner — Build & Publish Guide

A complete beginner's guide to building and publishing your game to the Google Play Store.

---

## 📋 What You Have

A full Android game with:
- 🌃 Night-time city rooftop with parallax scrolling buildings + moon
- 🏃 Animated parkour runner character
- 🎮 Tap to jump, swipe down to slide
- 🧱 Two obstacle types: walls (jump) and hanging pipes (slide)
- 🪙 Coins to collect, score counter, speed ramp
- 💾 Persistent high score saved to device
- 🎮 Menu screen + Game Over screen with retry

---

## STEP 1 — Install Android Studio (Free)

1. Go to: **https://developer.android.com/studio**
2. Download Android Studio for your OS (Windows / Mac / Linux)
3. Run the installer — use all default settings
4. On first launch, let it download the Android SDK (takes ~5–10 minutes)

---

## STEP 2 — Open the Project

1. Launch Android Studio
2. Click **"Open"** (not "New Project")
3. Navigate to the **CityRunner** folder you received
4. Click **OK** — Android Studio will sync the project (takes 1–2 minutes)
5. Wait until the bottom bar says **"Gradle sync finished"**

---

## STEP 3 — Run on Your Phone (Test First!)

### Enable Developer Mode on your Android phone:
1. Go to **Settings → About Phone**
2. Tap **Build Number** 7 times until you see "You are now a developer"
3. Go back to **Settings → Developer Options**
4. Enable **USB Debugging**

### Connect & Run:
1. Plug your phone into your computer with a USB cable
2. Tap **Allow** on your phone when it asks for USB debugging permission
3. In Android Studio, click the **▶ Run button** (green play button, top right)
4. Select your phone from the list
5. The game installs and launches on your phone! 🎉

---

## STEP 4 — Build the Release APK/AAB

To publish to Google Play, you need a signed **App Bundle (.aab)**.

### Create a Keystore (do this ONCE — keep it safe forever!):
1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Select **Android App Bundle** → Next
3. Click **Create new...** next to Key store path
4. Choose a safe location, set a password you'll remember
5. Fill in the Key alias (e.g. "cityrunner"), set passwords
6. Fill in your name/country → OK
7. Click **Next** → choose **release** → **Finish**

⚠️ **IMPORTANT**: Back up your keystore file! If you lose it, you can never update your app on Play Store.

The `.aab` file will appear in:
`app/release/app-release.aab`

---

## STEP 5 — Create a Google Play Developer Account

1. Go to: **https://play.google.com/console**
2. Sign in with your Google account
3. Pay the **$25 one-time registration fee**
4. Accept the Developer Agreement
5. Fill in your developer name and contact info

---

## STEP 6 — Create Your App on Play Console

1. Click **Create app**
2. App name: **City Runner**
3. Default language: English
4. App or Game: **Game**
5. Free or Paid: **Free** (recommended to start)
6. Accept the declarations → **Create app**

---

## STEP 7 — Fill In the Store Listing

Go to **Store presence → Main store listing**:

**Title:** City Runner: Rooftop Parkour

**Short description (80 chars):**
Run, jump and slide across city rooftops in this fast-paced endless runner!

**Full description:**
Race across the neon-lit rooftops of a sprawling city in this addictive endless runner!

🏃 TAP to jump over walls and obstacles
👇 SWIPE DOWN to slide under hanging pipes
🪙 Collect coins to boost your score
⚡ Speed increases as you progress — how far can you run?

Features:
• Stunning parallax night-city skyline
• Smooth 60fps gameplay
• High score saved to your device
• Simple one-hand controls

**Screenshots:** You need at least 2 phone screenshots.
- Run the game on your phone, take screenshots with your phone's screenshot button
- Upload them here

**App icon:** Take a screenshot of the app icon from your phone, or design one at canva.com

---

## STEP 8 — Complete Required Sections

In the left menu, complete these (green checkmarks = done):

**App content:**
- **Privacy Policy**: Create a free one at https://www.privacypolicygenerator.info/
  - App name: City Runner
  - No personal data collected
  - Copy the URL and paste it in Play Console

- **Ads**: Select "No ads"
- **Content rating**: Fill out the questionnaire → select "Everyone"
- **Target audience**: Select age 13+

**Pricing & distribution:**
- Select the countries you want to distribute to (or "All countries")

---

## STEP 9 — Upload Your App Bundle

1. Go to **Release → Production → Create new release**
2. Click **Upload** and select your `app-release.aab` file
3. Add release notes: "Initial release — endless rooftop parkour runner!"
4. Click **Save** → **Review release** → **Start rollout to Production**

---

## STEP 10 — Wait for Review ⏳

Google reviews new apps in **1–7 days**. You'll get an email when it's approved!

Once approved, your game is live on the Google Play Store. 🎉

---

## 🎨 Optional Customizations

Want to change the game? Here's where:

| What to change | File | What to edit |
|---|---|---|
| Game speed | `GameView.java` | `speed`, `maxSpeed`, `speedIncrease` |
| Jump height | `Player.java` | `jumpForce` (more negative = higher) |
| Obstacle frequency | `GameView.java` | `obstacleInterval` |
| Player colors | `Player.java` | color hex codes in `setupPaints()` |
| City colors | `CityBackground.java` | hex colors in `randomFarColor()` |
| App name | `res/values/strings.xml` | `app_name` |
| Package name | `app/build.gradle` | `applicationId` |

---

## 🆘 Common Issues

**"Gradle sync failed"** → Click **File → Invalidate Caches → Restart**

**"SDK not found"** → Open **SDK Manager** (Tools menu), install Android 14 (API 34)

**App crashes on phone** → Check **Logcat** tab at the bottom of Android Studio for the red error message

**Can't see my phone in device list** → Make sure USB debugging is enabled and you tapped "Allow" on the phone prompt

---

Good luck with your game! 🚀
