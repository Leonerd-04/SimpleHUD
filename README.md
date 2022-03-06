# SimpleHUD
SimpleHUD is a mod that adds a minimal, informational display to Minecraft's in game HUD, as a supplement to the F3 menu.
It eliminates the need to have the F3 menu open to check coordinates and framerates.

It is recommended to use this mod with TerraformersMC's ModMenu to gain access to config options.

## The HUD
SimpleHUD, as of now, displays the following:
- Average framerate and minimum framerate
- Coordinates
- Time

Besides just displaying this information, the color of the HUD itself acts as an indicator:
- The fps color changes to indicate low framerates
- The color of the clock changes to indicate when the player can sleep

## Compatibility
SimpleHUD only adds a rendering call to Minecraft's code, so it should generally be completely compatible with other mods.
Most notably, it is compatible with Sodium, Iris, and BetterF3.

While this mod is highly unlikely to break another mod in any way,
the HUD may end up obscuring or being obscured by other mods that add their own HUDs to Minecraft.
To remedy this, I want to add the ability for the user to choose the HUD's position at some point.

If you find an incompatibility with another mod, open an issue and I'll add it to a list of incompatibilities (currently empty).

## Goals
In the future I'd like to add more information to the HUD.
I see how this could cause clutter though, so I also want to let the user choose what information
the HUD displays, as well as in what order. As mentioned before, I'd also like to add positioning.
