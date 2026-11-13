import time, firebase_admin
from firebase_admin import credentials, messaging

firebase_admin.initialize_app(credentials.Certificate("service-account.json"))
TOKEN = "ekol-AN8QOiLgtI-jjYdGZ:APA91bEZp6mXtj1nZFBxv1PU-M96y6u6DMoaRcLCFSAMKLNSa3xeZKb_g3olJcTf9-FgskPSxFtbPvoIi0aaXTCJFevkS1xqwuoIMnfAneDHOfaBqEJWVt0"

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