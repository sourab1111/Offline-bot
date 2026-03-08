# 🤖 Offline AI — Android App

A fully offline AI assistant built entirely on Android using Acode + Termux.

---

## 📱 Features
- 💬 Chat UI with message bubbles
- 🎤 Voice input (speech-to-text)
- 🕒 Chat history with sessions
- 📚 Categories browser (9 knowledge topics)
- 🔍 Image analysis with ML Kit (on-device)
- 📖 Knowledge base with 30+ detailed Q&A entries
- 100% offline — no internet required

---

## 🏗 Project Structure

```
OfflineAI/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/offlineai/app/
│       │   ├── activities/
│       │   │   ├── SplashActivity.java
│       │   │   ├── MainActivity.java
│       │   │   ├── ChatActivity.java
│       │   │   ├── HistoryActivity.java
│       │   │   ├── CategoriesActivity.java
│       │   │   └── ImageAnalysisActivity.java
│       │   ├── adapters/
│       │   │   ├── ChatAdapter.java
│       │   │   ├── HistoryAdapter.java
│       │   │   └── CategoryAdapter.java
│       │   ├── database/
│       │   │   └── DatabaseHelper.java
│       │   ├── engine/
│       │   │   └── SearchEngine.java
│       │   ├── models/
│       │   │   ├── ChatMessage.java
│       │   │   ├── KnowledgeEntry.java
│       │   │   ├── Category.java
│       │   │   └── HistorySession.java
│       │   └── utils/
│       │       └── ResponseFormatter.java
│       ├── assets/knowledge/
│       │   ├── science.json
│       │   ├── technology.json
│       │   ├── health.json
│       │   ├── business_history.json
│       │   └── general.json
│       └── res/
│           ├── layout/ (all XML layouts)
│           ├── drawable/ (bubbles, buttons)
│           └── values/ (colors, strings, themes)
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🛠 How to Build (Termux on Android)

### Step 1: Install Termux tools
```bash
pkg update && pkg upgrade
pkg install git openjdk-17 wget unzip
```

### Step 2: Install Android SDK (Command Line Tools)
```bash
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
unzip commandlinetools-linux-*.zip
mv cmdline-tools latest
```

### Step 3: Set environment variables
Add these to `~/.bashrc` or `~/.zshrc`:
```bash
export ANDROID_HOME=~/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/build-tools/34.0.0
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk
```
Then run: `source ~/.bashrc`

### Step 4: Accept licenses and install SDK components
```bash
sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-34" "build-tools;34.0.0"
sdkmanager --sdk_root=$ANDROID_HOME --licenses
```

### Step 5: Copy this project to Termux
```bash
# If project is in shared storage:
cp -r /sdcard/OfflineAI ~/OfflineAI

# Or copy via Acode file manager
cd ~/OfflineAI
```

### Step 6: Make Gradle wrapper executable
```bash
chmod +x ./gradlew
```

### Step 7: Build the APK
```bash
# Debug build (for testing):
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Step 8: Install on your device
```bash
# If Android SDK platform-tools installed:
adb install app/build/outputs/apk/debug/app-debug.apk

# Or copy the APK to your Downloads and open it manually
cp app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/OfflineAI.apk
```

---

## 🧠 How to Expand the Knowledge Base

Add more entries to any JSON file in `assets/knowledge/` using this format:

```json
{
  "id": "unique_id",
  "keywords": ["your topic", "related words", "synonyms"],
  "answer": "Your detailed answer here.\n\nUse \\n for new lines.\n• Use bullets for lists",
  "category": "science"
}
```

**Categories available:**
- science
- technology
- health
- business
- history
- mathematics
- geography
- language
- general

**Tips for better matching:**
- Include multiple keyword variations (singular/plural, question forms)
- The more keywords, the better the matching
- Add both formal and casual question phrasings

---

## 🔍 How the Search Engine Works

1. User types a question
2. Keywords are extracted (stop words removed)
3. Keywords are matched against all entries in the database
4. Scoring: exact match = 3 pts, partial match = 1 pt, phrase match = 5 pts
5. Highest scoring entry wins
6. If score < 2, a fallback response is shown
7. Response is displayed with a realistic typing delay

---

## 📦 Dependencies Used

| Library | Purpose |
|---------|---------|
| Material Components | UI design |
| RecyclerView | Chat and list views |
| Room (SQLite) | Chat history storage |
| Glide | Image loading |
| ML Kit Image Labeling | On-device image detection |
| ML Kit Object Detection | Object recognition |
| Gson | JSON parsing |

---

## 🚀 Future Improvements

- [ ] Add more knowledge entries (aim for 1000+)
- [ ] Fuzzy string matching for typos
- [ ] TF-IDF ranking algorithm
- [ ] User-added custom Q&A entries
- [ ] Export/import knowledge base
- [ ] Text-to-speech for AI responses
- [ ] Dark mode
- [ ] Integrate TinyLlama for true LLM responses (requires powerful device)

---

## 💡 Tips

- The more JSON entries you add, the smarter it gets
- Copy from Wikipedia, textbooks, or any knowledge source
- Group related topics in the same JSON file
- Test edge cases: misspellings, short questions, multi-topic questions
