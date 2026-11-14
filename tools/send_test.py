import time, firebase_admin
from firebase_admin import credentials, messaging

firebase_admin.initialize_app(credentials.Certificate("service-account.json"))
TOKEN = "ekol-AN8QOiLgtI-jjYdGZ:APA91bEZp6mXtj1nZFBxv1PU-M96y6u6DMoaRcLCFSAMKLNSa3xeZKb_g3olJcTf9-FgskPSxFtbPvoIi0aaXTCJFevkS1xqwuoIMnfAneDHOfaBqEJWVt0"

msg = messaging.Message(
    token=TOKEN,
    data={
        "id": "notification_107",
        "title": "New Notification",
        "description": "This is a sample notification for the assignment.",
        "bannerImg": "https://i.pinimg.com/1200x/a6/eb/7d/a6eb7d447b58f2e213102692105eba9c.jpg",
        "timestamp": str(int(time.time() * 1000)),
    },
    android=messaging.AndroidConfig(priority="high"),
)
print(messaging.send(msg))