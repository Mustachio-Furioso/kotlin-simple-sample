import main.kotlin.ERROR
import main.kotlin.FruitStats
import main.kotlin.executeOrder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AmexTest {
    @Test
    internal fun orangeOverload() {
        val stats : FruitStats = executeOrder(arrayOf("Apple","Apple","Orange","Apple","Orange","Orange","Orange","Orange","Orange","Orange","Orange","Orange","Orange"))
        Assertions.assertEquals(ERROR.OUT_OF_STOCK,stats.error)
    }
    @Test
    internal fun potatoZergRush() {
        val stats : FruitStats = executeOrder(arrayOf("Yellow Potato","Sweet Potato","Baking Potato","Baby Potato","Mashed Potato"))
        Assertions.assertEquals(ERROR.COULD_NOT_PROCESS_PRODUCTS,stats.error)
    }
    @Test
    internal fun emptyShoppingCart() {
        val stats : FruitStats = executeOrder(arrayOf())
        Assertions.assertEquals(ERROR.NO_ORDERS_SUPPLIED,stats.error)
    }
    @Test
    internal fun quickShoppingTrip() {
        val stats : FruitStats = executeOrder(arrayOf("Apple","Orange","Orange"))
        Assertions.assertEquals(1.1,stats.netPrice)
    }
    @Test
    internal fun couponShopper() {
        val stats : FruitStats = executeOrder(arrayOf("Apple","Apple","Orange","Orange","Orange"))
        Assertions.assertEquals(1.1,stats.netPrice)
    }
}