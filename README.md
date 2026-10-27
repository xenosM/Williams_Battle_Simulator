# Williams Battle Simulator

A simple Java Swing battle simulator built for our group project. Two teams
face off across a battlefield with gold, troops, and a castle on each side.
Kept intentionally simple — built-in Java (AWT/Swing) only, no external
libraries.

## Running

From the project root in PowerShell:

```powershell
javac -d bin (Get-ChildItem src -Recurse -Filter *.java).FullName
java -cp bin App
```

Run from the project root, not `src/` — sprite paths (`assets/...`) are relative to
the working directory, so running from `src/` leaves them blank.

## Notes

- Battlefield grid: 30x30 tiles. Tiles use the available width and height, with
  a minimum size of 30 pixels in the normal view. Scroll to reach other parts of
  the board in a smaller window, or select Fit whole board for an overview.
  Each team's castle sits midway down its outer edge.
- Each team's `$` budget is displayed on the battlefield.
- Each team starts with $500 and an empty army.
- Choose a team, troop type, and group size in the buying bar. The total cost
  updates as you enter the size. Click an empty tile to buy and place the group.
- Team A places on the left half; Team B places on the right half. Castles,
  tile effects, and other groups occupy tiles and cannot be placed over.
- Prices per soldier: Infantry $10, Archer $15, Cavalry $25, Priest $15.
  One purchase creates one group, represented by one icon. Its badge always
  shows the number of surviving soldiers. Wider tiles also show the type (I/A/C/P).
- Hover over a group to see its type, soldier count, and health.
- Figures fit inside their own tile. Sprite scaling ignores transparent margins
  and preserves the image proportions. Team summaries stay visible when scrolling.
- Each troop class uses its own team sprite. Healing highlights the healed group
  and the priest's book. Tile effects report their latest event beneath the board.
- Invalid purchases show a message and do not spend gold. Once each team has
  at least one group, click Start Battle. Buying and placement then close.

## Checking purchases

After compiling the simulation, run these checks using the standard JDK:

```powershell
javac -cp bin -d bin tests/TroopBuyingTest.java tests/TeamIntegrationTest.java
java -cp bin TroopBuyingTest
java -cp bin TeamIntegrationTest
```
