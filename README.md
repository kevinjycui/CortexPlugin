![CortexPlugin](static/title.png)

Cortex API plugin for Minecraft. Uses Emotiv's Cortex API to make in-game adjustments in accordance to the user's brain-computer interface metrics. Demonstrates secure WebSocket connection protocol used in Cortex V2.

## Default Metrics and Attributes

| Entity | Attribute | Attribute Generic Name | Metric | Metric Code | Relationship |
| --- | --- | --- | --- | --- | --- |
| Player | Movement Speed | MOVEMENT_SPEED | Excitement | exc | Direct |
| Player | Knockback Resistance | KNOCKBACK_RESISTANCE | Relaxation | rel | Inverse |
| Player | Max Health | MAX_HEALTH | Relaxation | rel | Direct |
| Player | Attack Damage | ATTACK_DAMAGE | Focus | foc | Direct |
| Player | Attack Speed | ATTACK_SPEED | Focus | foc | Direct |
| Monster | Movement Speed | MOVEMENT_SPEED | Stress | str | Direct |
| Monster | Follow Range | FOLLOW_RANGE | Stress | str | Direct |
| Monster | Attack Damage | ATTACK_DAMAGE | Engagement | eng | Direct |

*Affects only the player with the Brain-computer Interface and Monsters within a 50 block radius of said player

![Screenshot](static/screenshot.png)

## Configuration

Download the latest stable release of `CortexPlugin.jar` to your `plugins` directory of your server. Add another directory `plugins/CortexPlugin` and create a file `plugins/cortex/credentials.json` containing the following (retrieved from your [cortex account](https://www.emotiv.com/my-account/cortex-apps/)):

```json
{
	"clientId": "your client ID here",
	"clientSecret": "your client secret here",
}
```
Your server directory should look like the following
```
.
├── plugins
│   ├── CortexPlugin
│   │   ├── credentials.json
│   └── CortexPlugin.jar
│   └── ...
└── ...
```

