# NotifyHub: Firebase Notification Inbox

NotifyHub is my submission for the Firebase Notification Inbox assignment. It's a native Android app that receives push notifications through Firebase Cloud Messaging, shows them in the notification bar, and keeps a local inbox of everything it has received. You can open any notification to see the details, and the app keeps track of what you've already read.

- Repository: https://github.com/PrincePratap/NotifyHub
- APK: [Download from Google Drive](https://drive.google.com/drive/folders/1joRGijTKGnWj3wFNZbHcHx71tsTX8I8W?usp=sharing)
- Package name: `com.cody.notifyhub`
- Notification channel: ID `assignment`, name `Assignment`

## Features

### Required features

- Receives FCM push notifications and shows them as system notifications with the app icon, title and description.
- Uses the `assignment` notification channel.
- Asks for the notification permission on Android 13 and above.
- Supports the payload from the assignment (`id`, `title`, `description`, `bannerImg`, `timestamp`), with `id` as the unique key.
- Inbox screen showing the banner image, title, description, time and read/unread state of each notification.
- Notifications and their read state are saved in a local database, so nothing is lost when the app is closed or restarted.
- Tapping a system notification opens the inbox. This also works when the app is already open.
- Tapping an item in the inbox opens the details screen (banner, title, description, time). Opening it marks it as read, and the inbox updates straight away.
- A notification with an `id` that's already in the inbox is ignored, so there are no duplicates.
- Missing or broken images, missing fields, duplicate IDs and app restarts are all handled without crashing.

### Extra features

- Unread count in the header
- Mark all as read
- Delete a single notification (asks for confirmation)
- Clear the whole inbox (asks for confirmation)
- Search by title or description
- Sorting: newest first, oldest first, or unread first
- Empty-state screens for an empty inbox and for searches with no results
- Relative times in the list ("5 min. ago") and the full date and time on the details screen
- Image placeholders while loading, when an image fails, and when there's no image at all
- The banner image also appears inside the system notification when you expand it. If the image is slow or broken, the notification still shows without it.
- A "Copy FCM token" option in the menu, so you can send test pushes to your device without opening Logcat

## Tech stack

| Area | What I used |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose with Material 3 |
| Architecture | MVVM with a repository |
| Async | Coroutines and Flow |
| Local database | Room |
| Dependency injection | Hilt |
| Push notifications | Firebase Cloud Messaging |
| Images | Coil 3 |
| Navigation | Navigation Compose |

Library versions are in `gradle/libs.versions.toml`.

## Setup and build

### What you need

- Android Studio (a recent stable version)
- A real Android phone, or an emulator with a **Google Play** system image. FCM doesn't work on emulators without Google Play services.
- A Google account for the Firebase Console

### Step 1: Clone the repo

```bash
git clone https://github.com/PrincePratap/NotifyHub.git
```

### Step 2: Get your own `google-services.json`

The app needs a `google-services.json` file to connect to Firebase. It isn't in the repo, so you'll need to create one from your own Firebase project. It takes about five minutes.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and sign in with your Google account.
2. Click **Create a project** (or **Add project**), give it any name, and click through the steps. You can turn Google Analytics off, since the app doesn't use it.
3. When the project is ready, you'll land on the project overview page. Click the **Android** icon to add an Android app. If you don't see it, go to ⚙️ **Project settings > General > Your apps > Add app > Android**.
4. Fill in the form:
   - **Android package name:** `com.cody.notifyhub`. It has to match exactly, otherwise the build will fail.
   - **App nickname:** anything you like (optional).
   - **Debug signing certificate SHA-1:** leave it empty. FCM doesn't need it.
5. Click **Register app**, then **Download google-services.json**.
6. Put the file in the **`app/`** folder of the project, next to `app/build.gradle.kts`:
   ```
   NotifyHub/
   ├── app/
   │   ├── build.gradle.kts
   │   └── google-services.json   ← here
   ├── build.gradle.kts
   └── settings.gradle.kts
   ```
   Tip: in Android Studio, switch the left panel from **Android** view to **Project** view to see this folder layout.
7. Back in the Firebase Console, skip the remaining "Add Firebase SDK" steps. The project already has everything set up. Click **Continue to console**.
8. Cloud Messaging is on by default. To double-check, go to ⚙️ **Project settings > Cloud Messaging** and make sure **Firebase Cloud Messaging API (V1)** says **Enabled**.

### Step 3: Build and run

1. Open the `NotifyHub` folder in Android Studio and wait for Gradle to sync.
2. Run the app on your phone or emulator.
3. Allow notifications when the app asks.

If the sync fails with `File google-services.json is missing`, the file is in the wrong folder. It must be inside `app/`, not in the project root.

### Building the APK

Go to **Build > Build App Bundle(s) / APK(s) > Build APK(s)**. When it finishes, the APK is in `app/build/outputs/apk/debug/app-debug.apk`.

### Testing with my APK instead

The APK on Google Drive is connected to my own Firebase project, so pushes have to come from that project. If you'd like to test with it, send me the email address you want to use and I'll add you to the Firebase project. You can then send test messages from the Firebase Console as described below.

## Sending test notifications

### Step 1: Get your device's FCM token

The token is the "address" of your phone. Firebase needs it to know where to send the notification.

- In the app, tap the **⋮** menu in the top-right corner and choose **Copy FCM token**. It's now on your phone's clipboard. Send it to yourself (in an email or a chat message, for example) so you can paste it on your computer.
- Or, if you're running from Android Studio, open **Logcat** and filter by `FCM_TOKEN`.

The token changes if you uninstall and reinstall the app, so copy it again after a reinstall.

### Option A: Firebase Console (no setup needed)

1. In the [Firebase Console](https://console.firebase.google.com/), open your project and go to **Messaging** (in the left menu, under **Run** or **Engage**).
2. Click **Create your first campaign** (or **New campaign**) and choose **Firebase Notification messages**.
3. Enter a **Notification title** and **Notification text**.
4. To send the assignment fields, scroll to **Additional options > Custom data** and add these keys and values:

   | Key | Example value |
      |---|---|
   | `id` | `notification_101` |
   | `title` | `New Notification` |
   | `description` | `This is a sample notification for the assignment.` |
   | `bannerImg` | `https://picsum.photos/600/300` |
   | `timestamp` | `1791873000000` |

5. Click **Send test message** (on the right of the first step), paste your FCM token into **Add an FCM registration token**, click **+**, tick the token and click **Test**.

Please read the first item under [Known limitations](#known-limitations) about Console messages: if the app is in the background, tap the notification so the app can save it.

### Option B: Python script (recommended)

`tools/send_test.py` sends the assignment payload as a **data-only** message. I used this for most of my testing, because data-only messages reach the app in every state: open, in the background, or swiped away from recents.

**One-time setup**

1. Install Python 3.11 or newer.
2. Install the Firebase Admin library:
   ```bash
   pip install firebase-admin
   ```
3. In the Firebase Console, go to ⚙️ **Project settings > Service accounts** and click **Generate new private key**. A JSON file downloads.
4. Rename it to `service-account.json` and move it into the `tools/` folder. The key must come from the **same** Firebase project as your `google-services.json`.

`service-account.json` is a private key. It's in `.gitignore`, so it won't be committed. Please don't share it or commit it.

**Sending a notification**

1. Open `tools/send_test.py` and replace `PASTE_TOKEN_HERE` with your FCM token.
2. Run it from the `tools` folder:
   ```bash
   cd tools
   python send_test.py
   ```
3. If it works, it prints a message ID like `projects/your-project/messages/0:1794...`, and the notification appears on your phone.

The script looks like this, in case you want to change the payload:

```python
import time
import firebase_admin
from firebase_admin import credentials, messaging

firebase_admin.initialize_app(credentials.Certificate("service-account.json"))
TOKEN = "PASTE_TOKEN_HERE"

msg = messaging.Message(
    token=TOKEN,
    data={
        "id": "notification_101",
        "title": "New Notification",
        "description": "This is a sample notification for the assignment.",
        "bannerImg": "https://picsum.photos/600/300",
        "timestamp": str(int(time.time() * 1000)),
    },
    android=messaging.AndroidConfig(priority="high"),
)
print(messaging.send(msg))
```

**If it doesn't work**

| Error | What it means |
|---|---|
| `FileNotFoundError: service-account.json` | You're not running the script from the `tools` folder, or the key file has a different name. |
| `Requested entity was not found` or `UNREGISTERED` | The token is wrong or out of date. Copy it again from the app. |
| `SenderId mismatch` | The service account key and `google-services.json` come from different Firebase projects. |
| Script succeeds but nothing appears | Check that notifications are allowed for the app, and that the phone has an internet connection. |

### Things worth trying

Change the `id` in the script (or in the Console's custom data) for each new notification.

| Try this | What should happen |
|---|---|
| Send a notification with the app open | It appears in the notification bar and at the top of the inbox |
| Press Home, then send one | It appears in the notification bar and is in the inbox when you open the app |
| Swipe the app away from recents, then send one | Same as above (data-only messages) |
| Tap the system notification | The inbox opens |
| Open a notification in the inbox | The details screen opens, and the item is marked as read |
| Send the same `id` twice | Only one entry in the inbox, and no second system notification |
| Use `https://example.com/banner.jpg` as `bannerImg` | A placeholder is shown, and the text is fine |
| Leave out `bannerImg`, `description` or `timestamp` | Fallbacks are used (see the table below), with no crash |
| Close the app completely and reopen it | Everything is still there, with the same read states |

### Payload

```json
{
  "id": "notification_101",
  "title": "New Notification",
  "description": "This is a sample notification for the assignment.",
  "bannerImg": "https://example.com/banner.jpg",
  "timestamp": "1791873000000"
}
```

| Field | What it's for | If it's missing or invalid |
|---|---|---|
| `id` | Unique ID, used to catch duplicates | The FCM message ID is used instead |
| `title` | Title | Shown as "New notification" |
| `description` | Body text | Left empty and shown as "No description" |
| `bannerImg` | Image URL, optional | A placeholder is shown and the text still appears |
| `timestamp` | Time in milliseconds, as a string | The time the message arrived is used |

## Assumptions

- `id` is the unique key. If a notification arrives with an `id` that's already saved, I keep the existing one (including its read state) and don't show a second system notification.
- If `id` is missing, I use the FCM message ID so the notification isn't lost.
- `timestamp` is in milliseconds and comes as a string, because FCM data values are always strings. If it's missing or isn't a number, I use the time the message arrived.
- `bannerImg` is optional. Only `http` and `https` URLs are treated as images.
- If you delete a notification and the same `id` arrives later, it's saved again as a new item.
- The inbox shows the newest notifications first unless you pick another sort order.
- The assignment doesn't ask for a backend, so test messages come from the Firebase Console or the script.

## Known limitations

- **Console messages in the background are only saved when tapped.** Messages from the Firebase Console are "notification" messages. When the app is in the background, Android shows them itself and doesn't run the app's code, so the app can only save them once the user taps the notification. If it's swiped away, it won't reach the inbox. Data-only messages, like the ones from the script, don't have this problem.
- **Console messages tapped from the background lose their title and body.** In that case Android only passes the custom data to the app. Add `title` and `description` as custom data, otherwise the item is saved as "New notification".
- **A force-stopped app doesn't receive pushes.** If the app is force-stopped from Settings, Android won't deliver FCM messages until it's opened again. Some phones' battery savers can cause similar delays.
- **The sort order isn't remembered.** It goes back to "Newest first" when the app restarts.
- **`FirebaseMessaging.getToken()` is deprecated** in the latest Firebase SDK. It still works fine.
- **Images on plain `http` won't load,** because Android blocks unencrypted traffic by default. The placeholder is shown instead.
- **The inbox is local to the device.** Nothing is synced between phones.

## Testing

I tested everything manually on a physical phone:

- Notifications received with the app open, in the background, and swiped away from recents
- Tapping a system notification opens the inbox
- Opening a notification marks it as read, and the inbox updates
- Sending the same `id` twice gives one inbox entry and no second notification
- Closing and reopening the app keeps all notifications and read states
- A broken image URL (including the sample `https://example.com/banner.jpg`) shows a placeholder with the text intact
- Missing `title`, `description`, `bannerImg` or `timestamp` uses the fallbacks without crashing
- Denying the notification permission doesn't crash the app, and notifications are still saved to the inbox
- Search, sorting, mark all as read, delete and clear inbox all work