# CritFix
This plugin fixes two cases where the client will see that they got a critical hit, but the server doesn't register it.

### Case 1
If you're hit by an opponent right before you hit, it's possible that your fall distance is set to 0 server-side. This issue is resolved by using the previous ticks fall distance if that ever happens.

### Case 2
This is a bit of a strange issue and the fix is defaulted to being off. When hitting an opponent directly before landing, it's possible that your client tells the server that you're on the ground, even if you still get the critical hit client-side.

This fix is disabled by default due to it likely being a problem with the client and not the server. If you want crits to more accurate based on what the client thinks, enable this.
