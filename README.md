# NotifyHub: Firebase Notification Inbox

NotifyHub is my submission for the Firebase Notification Inbox assignment. It's a native Android app that receives push notifications through Firebase Cloud Messaging, shows them in the notification bar, and keeps a local inbox of everything it has received. You can open any notification to see the details, and the app keeps track of what you've already read.

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

You'll need Android Studio and either a real Android phone or an emulator that has Google Play services (FCM doesn't work on emulators without it).

1. Clone the repo:
   ```bash
   git clone TODO add repo URL
   ```
2. Set up Firebase. `google-services.json` isn't committed, so you need your own:
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.cody.notifyhub`.
   - Download `google-services.json` and put it in the `app/` folder.
3. Open the project in Android Studio, wait for Gradle to sync, and run it.
4. Allow notifications when the app asks.

To build an APK, go to Build > Build App Bundle(s) / APK(s) > Build APK(s). The file is saved in `app/build/outputs/apk/debug/`.

## Sending test notifications

You'll need your device's FCM token first. In the app, open the menu (⋮) and tap "Copy FCM token". It's also printed in Logcat under the `FCM_TOKEN` tag.

### Using the Python script (recommended)

`tools/send_test.py` sends the assignment payload as a data-only message. I used this for most of my testing because data-only messages reach the app whether it's open, in the background or swiped away.

1. In the Firebase Console, go to Project settings > Service accounts > Generate new private key.
2. Save the key as `tools/service-account.json`. It's in `.gitignore`, so it won't be committed. Please don't commit it.
3. Paste your token into `TOKEN` in the script, then run:
   ```bash
   pip install firebase-admin
   cd tools
   python send_test.py
   ```

Change the `id` in the script each time you want a new notification. Send the same `id` again to check the duplicate handling.

### Using the Firebase Console

Go to Messaging, create a new notification, and use "Send test message" with your token. To send the assignment fields, add `id`, `title`, `description`, `bannerImg` and `timestamp` under Additional options > Custom data.

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
