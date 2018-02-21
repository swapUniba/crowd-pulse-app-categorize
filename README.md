# crowd-pulse-app-categorize
Simple Crowd Pulse plugin that assigns to each App (stored as personal data with the source "appinfo") its category, 
reading this information from Google Play.


You can specify the configuration option "calculate" with one of the following values:
- all: to get the category of all apps coming from the stream;
- new: to get the category of the apps with no category (property is null);

Default value: all.

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
      "plugin": "app-categorize",
      "config": {
        "calculate": "new"
      }
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