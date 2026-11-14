import time, firebase_admin
from firebase_admin import credentials, messaging

firebase_admin.initialize_app(credentials.Certificate("service-account.json"))
TOKEN = "dQx6FA85Q_2cUaWm8_35WS:APA91bH_OMBROBC68AR2mo-THU5J8EXBjSBdqpBcBa26e2PifB7kJN5enAqw58sMR5dIlhMwVsdNS1ZmUcFPO-6odUgLsJEjxArTHfOtzo4Y33qi3HUZFK4"

msg = messaging.Message(
    token=TOKEN,
    data={
        "id": "notification_105",
        # "title": "Test Notification",
        # "description": "This is a sample notification for the assignment.",
        # "bannerImg": "https://i.pinimg.com/736x/05/67/2c/05672ccbbefc7aa525d3e9432e88f37c.jpg",
        "timestamp": str(int(time.time() * 1000)),
    },
    android=messaging.AndroidConfig(priority="high"),
)
print(messaging.send(msg))