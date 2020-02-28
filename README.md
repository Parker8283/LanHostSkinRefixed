# Lan Host Skin (Re-)Fix(ed)
Fixes the bug where the LAN host's skin doesn't appear for all other connected players. This had to do with the way that
Mojang stored/retrieved skins starting in 1.7.6 and the way they were stored (or not stored) on the client side. If you
care about the details, you can read [this write-up][1] I made almost 5 years ago.

The reason this is "Refixed" is that I originally wrote this mod 5 years ago when the fix wasn't pulled into Forge for
1.7.10 (as 1.8 was just moving from beta to recommended, so I just missed the window). However, I was a big dumb-dumb
and it didn't work well and everyone complained (rightfully so). Since it was a number of years later, and people had
started to move to 1.12 as the new main modded version, I just deleted the old mod because I didn't care much about it
anymore. However, just recently I received a message on Reddit from someone looking for it. So I dug up the old source
code, used it as a reference, and rewrote the mod to be even better than it was before!

And now I'm uploading the code to GitHub so that others can reference it for...whatever it is they want to do. Not sure
what that would be considering Forge 1.13+ completely changed how coremods work. Well, transparency is a nice thing to
have in a coremod anyways.

[1]: https://github.com/MinecraftForge/MinecraftForge/pull/1826
