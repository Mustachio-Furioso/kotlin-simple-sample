package main.kotlin

import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess

data class Offer (
    val forFruit : String,
    val limit : Int,
    val triggerAt : Int,
    val discount : Double,
    val discountCount : Int
);
data class Fruit (
    val name : String,
    val price : Double,
    var stock : Int = 0
);
data class FruitStats ( //for the purposes of unit testing less barbarically
    val netPrice : Double = 0.0,
    val grossPrice : Double = 0.0,
    val discounts : HashMap<String,Double>? = null,
    val unknown : Stack<String>? = null,
    var error : ERROR? = null,
    var errorDetails : String? = null
);
enum class ERROR {
    OUT_OF_STOCK,
    COULD_NOT_PROCESS_PRODUCTS,
    NO_ORDERS_SUPPLIED
}

fun main(args:Array<String>) {
    val stats = executeOrder(args)

    if(stats.error != null) {
        die(stats.errorDetails, stats.error!!)
    }
    else { //I'm confused on step 3 - is this a kafka request? Multithreading? The latter seems overkill for such a simple program
        println("Order finished!")
        if (stats.unknown!!.isNotEmpty()) println("Could not find reference for ${stats.unknown.size} goods: ${stats.unknown.toString()}")
        println("Gross price: ${(String.format("$%.2f", stats.grossPrice))}")
        if (stats.discounts!!.isNotEmpty()) println("Discounts applied: ${stats.discounts.map { "${it.key}: ${it.value}" }}")
        println("Total price for [${args.joinToString(",")}] is ${String.format("$%.2f", stats.netPrice)}")
    }
}
fun executeOrder(fruitArr :Array<String>) : FruitStats {
    val prices = callPretendDatabase()
    val offers = callPretendDatabaseAgain()
    var totalPrice : Double = 0.0
    val fruitTally = HashMap<String,Int>()
    val unknownFruits = Stack<String>()
    val unknownFruit = Fruit("unknown", price = 0.0)

    var outOfStockFor : String? = null
    for(fruit in fruitArr) {
        val lowerFruit = fruit.lowercase()
        totalPrice += prices.getOrDefault(lowerFruit,unknownFruit).price
        if(!prices.containsKey(lowerFruit)) {
            unknownFruits.add(fruit)
        }
        else {
            if(prices[lowerFruit]!!.stock <= 0) outOfStockFor = fruit
            if(!fruitTally.containsKey(lowerFruit)) fruitTally[lowerFruit] = 0
            fruitTally[lowerFruit] = fruitTally[lowerFruit]!!.plus(1)
            prices[lowerFruit]!!.stock--
        }
    }
    if(!outOfStockFor.isNullOrEmpty()) return FruitStats(error = ERROR.OUT_OF_STOCK, errorDetails = outOfStockFor) //die("Ran out of stock for $outOfStockFor!")

    val grossPrice : Double = totalPrice

    val discountsApplied = HashMap<String,Double>()
    for (offer in offers) {
        if(fruitTally.containsKey(offer.forFruit)) {
            var totalTally = fruitTally[offer.forFruit]!!
            for(deal in 0..offer.limit) {
                if(totalTally >= offer.triggerAt) {
                    val totalDiscount: Double = prices[offer.forFruit]!!.price !! * offer.discount * offer.discountCount
                    totalPrice -= totalDiscount
                    totalTally -= offer.triggerAt
                    if(!discountsApplied.containsKey(offer.forFruit)) discountsApplied[offer.forFruit] = 0.0
                    discountsApplied[offer.forFruit] = discountsApplied[offer.forFruit]!!.plus(totalDiscount)
                }
                else {
                    break
                }
            }
        }
    }
    if(fruitTally.isNotEmpty()) {
        return FruitStats(
            netPrice = totalPrice,
            grossPrice = grossPrice,
            discounts = discountsApplied,
            unknown = unknownFruits
        )
    }
    else if (unknownFruits.isNotEmpty()) {
        return FruitStats(error = ERROR.COULD_NOT_PROCESS_PRODUCTS, errorDetails = unknownFruits.joinToString(","))
        //die("Could not process order!")
    }
    else {
        return FruitStats(error = ERROR.NO_ORDERS_SUPPLIED)
    }
}
fun callPretendDatabase() : HashMap<String, Fruit> { //e.g., select name,price,i.currentStock from fruit f join inventory i on f.id=i.fruit_id;
    return hashMapOf(
        "apple" to Fruit(name = "apple", price = .6, stock = 10),
        "orange" to Fruit(name = "orange", price = .25, stock = 5)
    )
}
fun callPretendDatabaseAgain() : List<Offer> { //e.g., select f.name,limit,trigger_at,discount,discountCount from offer o join fruit f on o.fruit_id=f.id;
    return listOf(
        Offer(
            forFruit = "apple",
            limit = 1,
            triggerAt = 2,
            discount = 1.0,
            discountCount = 1
        ),
        Offer(
            forFruit = "orange",
            limit = 1,
            triggerAt = 3,
            discount = 1.0,
            discountCount = 1
        )
    )
}
fun die(msg : String?, err : ERROR) {
    print("Order Failed: ${err.name}")
    if(!msg.isNullOrEmpty()) print(" [$msg]")
    println()
    exitProcess(1)
}