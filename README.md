# crowd-pulse-app-categorize
Simple Crowd Pulse plugin that assigns to each App (stored as personal data with the source "appinfo") its category, 
reading this information from Google Play.

Example of usage:

```json
{
  "process": {
    "name": "Test app categorize",
    "logs": "/opt/crowd-pulse/logs"
  },
  "nodes": {
    "fetch": {
      "plugin": "personaldata-fetch",
      "config": {
        "db": "personal_data",
        "source": "appinfo"
      }
    },
    "categorizer": {
      "plugin": "app-categorize"
    },
    "persistance": {
      "plugin": "personaldata-persist",
      "config": {
        "db": "personal_data"
      }
    }
  },
  "edges": {
    "fetch": [
      "categorizer"
    ],
    "categorizer": [
      "persistance"
    ]
  }
}
```