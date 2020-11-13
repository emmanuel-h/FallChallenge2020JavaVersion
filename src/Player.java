import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            List<Potion> potions = new ArrayList<>();
            List<Spell> spells = new ArrayList<>();
            Witch witch;
            Witch opponentWitch;

            int actionCount = in.nextInt(); // the number of spells and recipes in play
            for (int i = 0; i < actionCount; i++) {
                int actionId = in.nextInt(); // the unique ID of this spell or recipe
                String actionType = in.next(); // in the first league: BREW; later: CAST, OPPONENT_CAST, LEARN, BREW
                int delta0 = in.nextInt(); // tier-0 ingredient change
                int delta1 = in.nextInt(); // tier-1 ingredient change
                int delta2 = in.nextInt(); // tier-2 ingredient change
                int delta3 = in.nextInt(); // tier-3 ingredient change
                int price = in.nextInt(); // the price in rupees if this is a potion
                int tomeIndex = in.nextInt(); // in the first two leagues: always 0; later: the index in the tome if this is a tome spell, equal to the read-ahead tax
                int taxCount = in.nextInt(); // in the first two leagues: always 0; later: the amount of taxed tier-0 ingredients you gain from learning this spell
                boolean castable = in.nextInt() != 0; // in the first league: always 0; later: 1 if this is a castable player spell
                boolean repeatable = in.nextInt() != 0; // for the first two leagues: always 0; later: 1 if this is a repeatable player spell
                switch (actionType) {
                    case "BREW":
                        potions.add(new Potion(actionId, delta0, delta1, delta2, delta3, price));
                        break;
                    case "CAST":
                        spells.add(new Spell(actionId, delta0, delta1, delta2, delta3, price, tomeIndex, taxCount, castable, repeatable));
                }
            }
            int inv0 = in.nextInt(); // tier-0 ingredients in inventory
            int inv1 = in.nextInt();
            int inv2 = in.nextInt();
            int inv3 = in.nextInt();
            int score = in.nextInt(); // amount of rupees
            witch = new Witch(inv0, inv1, inv2, inv3, score);

            inv0 = in.nextInt(); // tier-0 ingredients in inventory
            inv1 = in.nextInt();
            inv2 = in.nextInt();
            inv3 = in.nextInt();
            score = in.nextInt(); // amount of rupees
            opponentWitch = new Witch(inv0, inv1, inv2, inv3, score);

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            String action = "REST";
            Potion potion = getBestPotion(potions, witch);
            if (potion.brewable(witch)) {
                action = "BREW " + potion.id;
            } else {
                int[] missingIngredients = {
                        potion.tier0Ingredient > 0 ? 0 : ((witch.tierOInventory + potion.tier0Ingredient) > 0 ? 0 : Math.abs(potion.tier0Ingredient + witch.tierOInventory)),
                        potion.tier1Ingredient > 0 ? 0 : ((witch.tier1Inventory + potion.tier1Ingredient) > 0 ? 0 : Math.abs(potion.tier1Ingredient + witch.tier1Inventory)),
                        potion.tier2Ingredient > 0 ? 0 : ((witch.tier2Inventory + potion.tier2Ingredient) > 0 ? 0 : Math.abs(potion.tier2Ingredient + witch.tier2Inventory)),
                        potion.tier3Ingredient > 0 ? 0 : ((witch.tier3Inventory + potion.tier3Ingredient) > 0 ? 0 : Math.abs(potion.tier3Ingredient + witch.tier3Inventory))
                };
                System.err.println("potion missing ingredients ");
                System.err.println(Arrays.toString(missingIngredients));
                System.err.println(potion);
                System.err.println(witch);

                Optional<Spell> bestSpell = getBestSpell(spells, witch, missingIngredients);
                action = bestSpell.map(spell -> "CAST " + spell.id).orElse("REST");
            }


            // in the first league: BREW <id> | WAIT; later: BREW <id> | CAST <id> [<times>] | LEARN <id> | REST | WAIT
            System.out.println(action);
        }
    }

    static Potion getBestPotion(List<Potion> potions, Witch witch) {
        return potions.stream().max(Comparator.comparing(p -> p.appeal(witch))).get();
    }

    static Optional<Spell> getBestSpell(List<Spell> spells, Witch witch, int[] missingIngredients) {
        Optional<Spell> bestSpell = Optional.empty();

        while (!spells.isEmpty()) {
            Spell spell = spells.stream().max(Comparator.comparing(s -> s.addUsefulIngredient(missingIngredients))).get();
            if (spell.enoughIngredient(witch) && spell.castable && spell.addUsefulIngredient(missingIngredients) > 0) {
                bestSpell = Optional.of(spell);
                break;
            } else {
                System.err.println("la");
                System.err.println(spell);
                System.err.println(Arrays.toString(missingIngredients));
                missingIngredients[0] = spell.tierOIngredient > 0 ? 0 : ((witch.tierOInventory + spell.tierOIngredient) > 0 ? 0 : Math.abs(witch.tierOInventory + spell.tierOIngredient));
                missingIngredients[1] = spell.tier1Ingredient > 0 ? 0 : ((witch.tier1Inventory + spell.tier1Ingredient) > 0 ? 0 : Math.abs(witch.tier1Inventory + spell.tier1Ingredient));
                missingIngredients[2] = spell.tier2Ingredient > 0 ? 0 : ((witch.tier2Inventory + spell.tier2Ingredient) > 0 ? 0 : Math.abs(witch.tier2Inventory + spell.tier2Ingredient));
                missingIngredients[3] = spell.tier3Ingredient > 0 ? 0 : ((witch.tier3Inventory + spell.tier3Ingredient) > 0 ? 0 : Math.abs(witch.tier3Inventory + spell.tier3Ingredient));
                spells.remove(spell);
            }
        }
        return bestSpell;
    }
}

class Potion {
    public int id;
    public int tier0Ingredient;
    public int tier1Ingredient;
    public int tier2Ingredient;
    public int tier3Ingredient;
    public int price;

    public Potion(int id, int tier0Ingredient, int tier1Ingredient, int tier2Ingredient, int tier3Ingredient, int price) {
        this.id = id;
        this.tier0Ingredient = tier0Ingredient;
        this.tier1Ingredient = tier1Ingredient;
        this.tier2Ingredient = tier2Ingredient;
        this.tier3Ingredient = tier3Ingredient;
        this.price = price;
    }

    public boolean brewable(Witch witch) {
        return witch.tierOInventory + this.tier0Ingredient >= 0
                && witch.tier1Inventory + this.tier1Ingredient >= 0
                && witch.tier2Inventory + this.tier2Ingredient >= 0
                && witch.tier3Inventory + this.tier3Ingredient >= 0
                ;
    }

    public int appeal(Witch witch) {
        return this.price
                + Math.min(witch.tierOInventory + tier0Ingredient, 0)
                + 2*Math.min(witch.tier1Inventory + tier1Ingredient, 0)
                + 4*Math.min(witch.tier2Inventory + tier2Ingredient, 0)
                + 8*Math.min(witch.tier3Inventory + tier3Ingredient, 0);
    }

    @Override
    public String toString() {
        return "Potion{" +
                "id=" + id +
                ", tierOIngredient=" + tier0Ingredient +
                ", tier1Ingredient=" + tier1Ingredient +
                ", tier2Ingredient=" + tier2Ingredient +
                ", tier3Ingredient=" + tier3Ingredient +
                ", price=" + price +
                '}';
    }
}

class Witch {
    public int tierOInventory;
    public int tier1Inventory;
    public int tier2Inventory;
    public int tier3Inventory;
    public int score;

    public Witch(int tierOInventory, int tier1Inventory, int tier2Inventory, int tier3Inventory, int score) {
        this.tierOInventory = tierOInventory;
        this.tier1Inventory = tier1Inventory;
        this.tier2Inventory = tier2Inventory;
        this.tier3Inventory = tier3Inventory;
        this.score = score;
    }

    @Override
    public String toString() {
        return "Witch{" +
                "tierOInventory=" + tierOInventory +
                ", tier1Inventory=" + tier1Inventory +
                ", tier2Inventory=" + tier2Inventory +
                ", tier3Inventory=" + tier3Inventory +
                ", score=" + score +
                '}';
    }
}

class Spell {
    public int id;
    public int tierOIngredient;
    public int tier1Ingredient;
    public int tier2Ingredient;
    public int tier3Ingredient;
    public int price;
    public int tomeIndex;
    public int taxCount;
    public boolean castable;
    public boolean repeatable;

    public Spell(int id, int tierOIngredient, int tier1Ingredient, int tier2Ingredient, int tier3Ingredient, int price, int tomeIndex, int taxCount, boolean castable, boolean repeatable) {
        this.id = id;
        this.tierOIngredient = tierOIngredient;
        this.tier1Ingredient = tier1Ingredient;
        this.tier2Ingredient = tier2Ingredient;
        this.tier3Ingredient = tier3Ingredient;
        this.price = price;
        this.tomeIndex = tomeIndex;
        this.taxCount = taxCount;
        this.castable = castable;
        this.repeatable = repeatable;
    }

    public boolean enoughIngredient(Witch witch) {
        return
                (Math.abs(this.tierOIngredient) <= witch.tierOInventory || this.tierOIngredient > 0)
                && (Math.abs(this.tier1Ingredient) <= witch.tier1Inventory || this.tier1Ingredient > 0)
                && (Math.abs(this.tier2Ingredient) <= witch.tier2Inventory || this.tier2Ingredient > 0)
                && (Math.abs(this.tier3Ingredient) <= witch.tier3Inventory || this.tier3Ingredient > 0)
                ;
    }

    public int addUsefulIngredient(int[] missingIngredients) {
        int addedIngredients = 0;
        if (missingIngredients[0] > 0 && this.tierOIngredient > 0) { addedIngredients += tierOIngredient; }
        if (missingIngredients[1] > 0 && this.tier1Ingredient > 0) { addedIngredients += tier1Ingredient; }
        if (missingIngredients[2] > 0 && this.tier2Ingredient > 0) { addedIngredients += tier2Ingredient; }
        if (missingIngredients[3] > 0 && this.tier3Ingredient > 0) { addedIngredients += tier3Ingredient; }
        return addedIngredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spell spell = (Spell) o;
        return id == spell.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Spell{" +
                "id=" + id +
                ", tierOIngredient=" + tierOIngredient +
                ", tier1Ingredient=" + tier1Ingredient +
                ", tier2Ingredient=" + tier2Ingredient +
                ", tier3Ingredient=" + tier3Ingredient +
                ", price=" + price +
                ", tomeIndex=" + tomeIndex +
                ", taxCount=" + taxCount +
                ", castable=" + castable +
                ", repeatable=" + repeatable +
                '}';
    }
}