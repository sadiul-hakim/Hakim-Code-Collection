In a multi-vendor e-commerce system, inventory is more complicated than in a normal single-store e-commerce system because different sellers can sell the same product, each seller may have different stock, warehouses, prices, and fulfillment rules.

The key idea is:

Product is global; inventory belongs to a seller/warehouse combination.

1. Simple example 

Imagine your marketplace sells an iPhone 15.

Three vendors sell it:

Vendor Product Warehouse Stock Vendor A iPhone 15 Dhaka 20 Vendor B iPhone 15 Khulna 8 Vendor C iPhone 15 Dhaka 35 

There is one product definition:

Product -------- iPhone 15 128GB Black 

But there are three separate inventory records.

Seller A → iPhone 15 → Warehouse A → 20 Seller B → iPhone 15 → Warehouse B → 8 Seller C → iPhone 15 → Warehouse C → 35 

You should generally not put stock = 63 directly on the Product table.

2. A useful data model 

A simplified Django-style model structure could look like:

Vendor │ ├── Warehouse │ │ │ └── Inventory │ │ │ └── ProductVariant │ └── Listings │ └── ProductVariant 

For example:

class Vendor(models.Model): name = models.CharField(max_length=200) class Warehouse(models.Model): vendor = models.ForeignKey(Vendor, on_delete=models.CASCADE) name = models.CharField(max_length=200) class Product(models.Model): name = models.CharField(max_length=200) class ProductVariant(models.Model): product = models.ForeignKey(Product, on_delete=models.CASCADE) sku = models.CharField(max_length=100, unique=True) class Inventory(models.Model): warehouse = models.ForeignKey(Warehouse, on_delete=models.CASCADE) variant = models.ForeignKey(ProductVariant, on_delete=models.CASCADE) quantity = models.PositiveIntegerField() reserved_quantity = models.PositiveIntegerField(default=0) 

Then:

available_quantity = quantity - reserved_quantity 3. Why do we need reserved_quantity? 

This is extremely important.

Suppose Vendor A has:

quantity = 10 reserved = 0 available = 10 

Customer 1 buys 7.

The payment process may take a few seconds.

During that time, Customer 2 might also try to buy 7.

If you only have:

quantity = 10 

both customers might successfully order.

Now you've sold:

7 + 7 = 14 

when you only had 10.

Instead, when Customer 1 checks out:

quantity = 10 reserved = 7 available = 3 

Customer 2 can therefore only purchase 3.

4. Inventory has several states 

A production marketplace usually needs to distinguish several concepts.

For example:

On hand ↓ Reserved ↓ Committed ↓ Shipped ↓ Delivered 

A more useful conceptual model is:

On-hand stock │ ├── Available │ └── Reserved 

When an order is placed:

Available → Reserved 

When the order is successfully fulfilled:

Reserved → Sold 

If the order is cancelled:

Reserved → Available 

If inventory is returned:

Sold → Returned → Available 5. Inventory is not the same thing as a product listing 

This distinction becomes very important in multi-vendor systems.

Suppose:

Product: Samsung Galaxy S25 

Vendor A might sell it for:

৳90,000 

Vendor B:

৳92,000 

Vendor C:

৳88,000 

So you might have:

Product │ ├── Vendor A Listing │ ├── price = 90,000 │ └── inventory = 20 │ ├── Vendor B Listing │ ├── price = 92,000 │ └── inventory = 5 │ └── Vendor C Listing ├── price = 88,000 └── inventory = 12 

This is why marketplaces often have a concept similar to a seller listing/offer.

6. Product vs Variant 

Inventory should usually belong to the variant/SKU, not merely the product.

For example:

T-Shirt │ ├── Red / Small ├── Red / Medium ├── Red / Large ├── Blue / Small ├── Blue / Medium └── Blue / Large 

Each can have different inventory:

Red / Small → 10 Red / Medium → 25 Red / Large → 4 Blue / Small → 18 Blue / Medium → 30 Blue / Large → 7 

So normally:

Inventory → ProductVariant 

rather than:

Inventory → Product 7. What happens when a customer orders? 

Suppose:

Vendor A Warehouse Dhaka iPhone 15 Available = 20 

Customer purchases:

quantity = 3 

Your transaction should essentially do:

BEGIN lock inventory row check available >= 3 reserved += 3 create order create order item COMMIT 

Conceptually:

Before: quantity = 20 reserved = 0 available = 20 After: quantity = 20 reserved = 3 available = 17 

The database transaction and row locking are very important here.

In Django/PostgreSQL, you would commonly use something like:

with transaction.atomic(): inventory = ( Inventory.objects .select_for_update() .get(...) ) available = ( inventory.quantity - inventory.reserved_quantity ) if available < requested_quantity: raise InsufficientStock() inventory.reserved_quantity += requested_quantity inventory.save() 

The important part isn't the exact code; it's that two concurrent checkout requests shouldn't be allowed to modify the same inventory row blindly.

8. What if one customer buys from multiple vendors? 

This is where multi-vendor inventory becomes interesting.

Customer:

Cart │ ├── iPhone → Vendor A ├── T-Shirt → Vendor B └── Headphones → Vendor C 

The marketplace may create:

Parent Order #1001 │ ├── Vendor Order #1001-A │ └── iPhone │ ├── Vendor Order #1001-B │ └── T-Shirt │ └── Vendor Order #1001-C └── Headphones 

The customer sees:

Order #1001 

But internally each vendor manages its own:

order inventory shipment commission refund fulfillment 

This separation is extremely useful.

9. Multiple warehouses 

Now imagine Vendor A becomes large.

Vendor A Dhaka Warehouse iPhone → 20 Chittagong Warehouse iPhone → 15 Khulna Warehouse iPhone → 10 

Total inventory:

20 + 15 + 10 = 45 

But when someone orders from Dhaka, you don't necessarily want to take stock from Khulna.

So inventory can be represented as:

Vendor ↓ Warehouse ↓ Inventory ↓ SKU 

This gives you:

Vendor A ├── Dhaka │ ├── SKU-001 → 20 │ └── SKU-002 → 50 │ └── Khulna ├── SKU-001 → 10 └── SKU-002 → 30 10. Inventory movements 

For a serious system, don't rely exclusively on a single quantity number.

You should consider maintaining an inventory ledger.

For example:

InventoryTransaction +100 Stock received -5 Customer order -2 Damaged +10 Customer return -3 Manual adjustment 

So you have:

Inventory │ └── InventoryTransaction │ ├── PURCHASE ├── SALE ├── RETURN ├── DAMAGE ├── ADJUSTMENT └── TRANSFER 

This gives you an audit trail.

If a seller says:

"I had 100 products yesterday. Why do I have only 73 today?"

you can investigate the inventory movements.

11. Stock transfer 

Suppose:

Dhaka warehouse = 100 Khulna warehouse = 20 

Vendor transfers 30 to Khulna.

You shouldn't simply change:

Dhaka = 70 Khulna = 50 

without recording why.

Instead:

TRANSFER_OUT Dhaka -30 TRANSFER_IN Khulna +30 

Now your system has an audit history.

12. The architecture I would recommend for a small/medium marketplace 

You do not need an extremely complicated inventory system from day one.

A sensible architecture is:

Product │ ProductVariant │ SellerListing / \ Vendor Price │ Warehouse │ Inventory │ ┌──────────┴──────────┐ │ │ quantity reserved_quantity │ └──── InventoryLedger 

And orders:

Customer │ Cart │ Order │ ├── VendorOrder A │ └── OrderItems │ ├── VendorOrder B │ └── OrderItems │ └── VendorOrder C └── OrderItems The most important principles 

For a multi-vendor e-commerce system, I'd keep these principles in mind:

Product ≠ inventory ProductVariant/SKU is usually the inventory unit Inventory belongs to a vendor/warehouse Seller listing/offer contains seller-specific price and selling information Separate available stock from reserved stock Use database transactions + row locking when reserving stock Keep an inventory movement/ledger for auditing Separate customer-facing orders from vendor-specific fulfillment orders Don't introduce sharding or distributed inventory unnecessarily PostgreSQL + proper indexes + transactions is enough for a surprisingly large marketplace 

For the kind of small-to-medium Django marketplace you were asking about earlier, I'd start with PostgreSQL + normalized inventory tables + select_for_update() + Redis only for caching, rather than trying to build a distributed inventory system.

---

Absolutely. Below is a practical Django/PostgreSQL design for a multi-vendor marketplace. It covers the main points from the previous explanation without introducing unnecessary microservices or distributed inventory complexity.

I'll keep it reasonably production-oriented, but not so complicated that it's difficult to understand.

1. Overall model structure 

The relationships will look roughly like this:

Vendor │ ├── Warehouse │ │ │ └── Inventory ───── ProductVariant │ └── SellerListing ───────── ProductVariant │ Product ──────────────────────┘ Customer │ └── Order │ ├── VendorOrder ─── Vendor │ │ │ └── OrderItem ─── SellerListing │ └── VendorOrder ─── Vendor 

And inventory movements:

Inventory │ └── InventoryTransaction ├── PURCHASE ├── SALE ├── RESERVATION ├── RELEASE ├── RETURN ├── DAMAGE ├── ADJUSTMENT └── TRANSFER 2. Models 

A reasonable models.py could be:

from django.conf import settings from django.core.validators import MinValueValidator from django.db import models class TimeStampedModel(models.Model): created_at = models.DateTimeField(auto_now_add=True) updated_at = models.DateTimeField(auto_now=True) class Meta: abstract = True # ============================================================ # VENDOR # ============================================================ class Vendor(TimeStampedModel): name = models.CharField(max_length=200) slug = models.SlugField(unique=True) is_active = models.BooleanField(default=True) def __str__(self): return self.name # ============================================================ # PRODUCT # ============================================================ class Product(TimeStampedModel): name = models.CharField(max_length=255) slug = models.SlugField(unique=True) description = models.TextField(blank=True) is_active = models.BooleanField(default=True) def __str__(self): return self.name class ProductVariant(TimeStampedModel): """ The actual SKU that is sold. Example: Product: T-Shirt Variants: TSHIRT-RED-S TSHIRT-RED-M TSHIRT-BLUE-L """ product = models.ForeignKey( Product, on_delete=models.CASCADE, related_name="variants", ) sku = models.CharField( max_length=100, unique=True, ) name = models.CharField( max_length=255, help_text="Example: Red / Medium", ) is_active = models.BooleanField(default=True) def __str__(self): return f"{self.product.name} - {self.name}" # ============================================================ # WAREHOUSE # ============================================================ class Warehouse(TimeStampedModel): vendor = models.ForeignKey( Vendor, on_delete=models.CASCADE, related_name="warehouses", ) name = models.CharField(max_length=200) address = models.TextField(blank=True) is_active = models.BooleanField(default=True) def __str__(self): return f"{self.vendor.name} - {self.name}" # ============================================================ # SELLER LISTING # ============================================================ class SellerListing(TimeStampedModel): """ A vendor's offer for a particular product variant. Example: iPhone 15 / 128GB / Black Vendor A -> 90,000 Vendor B -> 92,000 Vendor C -> 88,000 """ vendor = models.ForeignKey( Vendor, on_delete=models.CASCADE, related_name="listings", ) variant = models.ForeignKey( ProductVariant, on_delete=models.CASCADE, related_name="listings", ) price = models.DecimalField( max_digits=12, decimal_places=2, validators=[MinValueValidator(0)], ) is_active = models.BooleanField(default=True) class Meta: constraints = [ models.UniqueConstraint( fields=["vendor", "variant"], name="unique_vendor_variant_listing", ), ] def __str__(self): return f"{self.vendor} - {self.variant}" # ============================================================ # INVENTORY # ============================================================ class Inventory(TimeStampedModel): """ Stock belonging to one vendor's warehouse for one product variant. """ warehouse = models.ForeignKey( Warehouse, on_delete=models.CASCADE, related_name="inventory", ) variant = models.ForeignKey( ProductVariant, on_delete=models.CASCADE, related_name="inventory_records", ) quantity = models.PositiveIntegerField(default=0) reserved_quantity = models.PositiveIntegerField(default=0) class Meta: constraints = [ models.UniqueConstraint( fields=["warehouse", "variant"], name="unique_warehouse_variant_inventory", ), ] @property def available_quantity(self): return self.quantity - self.reserved_quantity def __str__(self): return ( f"{self.warehouse} - " f"{self.variant} - " f"{self.quantity}" ) # ============================================================ # INVENTORY LEDGER # ============================================================ class InventoryTransactionType(models.TextChoices): PURCHASE = "PURCHASE", "Purchase" SALE = "SALE", "Sale" RESERVATION = "RESERVATION", "Reservation" RELEASE = "RELEASE", "Release" RETURN = "RETURN", "Return" DAMAGE = "DAMAGE", "Damage" ADJUSTMENT = "ADJUSTMENT", "Adjustment" TRANSFER_IN = "TRANSFER_IN", "Transfer In" TRANSFER_OUT = "TRANSFER_OUT", "Transfer Out" class InventoryTransaction(TimeStampedModel): """ Immutable-ish audit trail of inventory changes. """ inventory = models.ForeignKey( Inventory, on_delete=models.PROTECT, related_name="transactions", ) transaction_type = models.CharField( max_length=30, choices=InventoryTransactionType.choices, ) quantity = models.IntegerField() reference = models.CharField( max_length=255, blank=True, help_text="Order ID, transfer ID, etc.", ) note = models.TextField(blank=True) class Meta: ordering = ["-created_at"] def __str__(self): return ( f"{self.transaction_type}: " f"{self.quantity}" ) 3. Why quantity and reserved_quantity? 

Suppose:

quantity = 100 reserved_quantity = 20 

Then:

available = 80 

So:

inventory.available_quantity 

returns:

80 

The important thing is that reserved stock hasn't physically disappeared yet.

For example:

100 physical units 20 reserved for customers 80 available for new orders 4. Orders 

Now let's create the order models.

# ============================================================ # ORDER # ============================================================ class OrderStatus(models.TextChoices): PENDING = "PENDING", "Pending" CONFIRMED = "CONFIRMED", "Confirmed" PROCESSING = "PROCESSING", "Processing" COMPLETED = "COMPLETED", "Completed" CANCELLED = "CANCELLED", "Cancelled" class Order(TimeStampedModel): customer = models.ForeignKey( settings.AUTH_USER_MODEL, on_delete=models.PROTECT, related_name="orders", ) status = models.CharField( max_length=30, choices=OrderStatus.choices, default=OrderStatus.PENDING, ) total_amount = models.DecimalField( max_digits=12, decimal_places=2, default=0, ) def __str__(self): return f"Order #{self.pk}" # ============================================================ # VENDOR ORDER # ============================================================ class VendorOrderStatus(models.TextChoices): PENDING = "PENDING", "Pending" CONFIRMED = "CONFIRMED", "Confirmed" PROCESSING = "PROCESSING", "Processing" SHIPPED = "SHIPPED", "Shipped" DELIVERED = "DELIVERED", "Delivered" CANCELLED = "CANCELLED", "Cancelled" class VendorOrder(TimeStampedModel): """ One customer's order can produce multiple VendorOrders. """ order = models.ForeignKey( Order, on_delete=models.CASCADE, related_name="vendor_orders", ) vendor = models.ForeignKey( Vendor, on_delete=models.PROTECT, related_name="vendor_orders", ) status = models.CharField( max_length=30, choices=VendorOrderStatus.choices, default=VendorOrderStatus.PENDING, ) subtotal = models.DecimalField( max_digits=12, decimal_places=2, default=0, ) class Meta: constraints = [ models.UniqueConstraint( fields=["order", "vendor"], name="unique_order_vendor", ), ] def __str__(self): return f"VendorOrder #{self.pk}" # ============================================================ # ORDER ITEM # ============================================================ class OrderItem(TimeStampedModel): vendor_order = models.ForeignKey( VendorOrder, on_delete=models.CASCADE, related_name="items", ) listing = models.ForeignKey( SellerListing, on_delete=models.PROTECT, related_name="order_items", ) variant = models.ForeignKey( ProductVariant, on_delete=models.PROTECT, related_name="order_items", ) quantity = models.PositiveIntegerField() unit_price = models.DecimalField( max_digits=12, decimal_places=2, ) total_price = models.DecimalField( max_digits=12, decimal_places=2, ) def __str__(self): return f"{self.variant} x {self.quantity}" 5. Why store unit_price in OrderItem? 

This is important.

Suppose today:

iPhone = ৳90,000 

Customer buys it.

Tomorrow:

iPhone = ৳95,000 

If your order only references:

listing.price 

then an old order could suddenly appear to cost:

৳95,000 

instead of:

৳90,000 

Therefore, the order needs a price snapshot:

OrderItem ---------------- unit_price = 90,000 quantity = 1 total_price = 90,000 

Same principle applies to product information that needs to remain historically accurate.

6. Reserving inventory correctly 

This is probably the most important part.

Suppose:

Stock = 10 Reserved = 0 

Two customers simultaneously try to buy 7.

We need to prevent:

Customer A → 7 Customer B → 7 

from both succeeding.

Use:

select_for_update() 

inside:

transaction.atomic() 

For example:

from django.db import transaction from .models import ( Inventory, InventoryTransaction, InventoryTransactionType, ) @transaction.atomic def reserve_inventory( *, inventory_id: int, quantity: int, reference: str = "", ): inventory = ( Inventory.objects .select_for_update() .select_related("variant", "warehouse") .get(pk=inventory_id) ) if quantity <= 0: raise ValueError("Quantity must be greater than zero.") available = ( inventory.quantity - inventory.reserved_quantity ) if available < quantity: raise ValueError( f"Insufficient stock. " f"Available: {available}, " f"requested: {quantity}" ) inventory.reserved_quantity += quantity inventory.save( update_fields=[ "reserved_quantity", "updated_at", ] ) InventoryTransaction.objects.create( inventory=inventory, transaction_type=InventoryTransactionType.RESERVATION, quantity=-quantity, reference=reference, note="Inventory reserved for order", ) return inventory 7. Why select_for_update() matters 

Imagine:

Stock = 10 Reserved = 0 

Request A:

read → available = 10 

Request B:

read → available = 10 

Both think they can buy 7.

That's a race condition.

With:

.select_for_update() 

PostgreSQL effectively does:

Request A ↓ LOCK inventory row ↓ update reserved = 7 ↓ COMMIT ↓ unlock Request B ↓ waits ↓ gets row ↓ sees reserved = 7 ↓ available = 3 ↓ fails 

That's exactly what we want.

8. Releasing a reservation 

Suppose the customer doesn't pay or cancels the order.

We need:

reserved -= quantity 

Code:

@transaction.atomic def release_inventory( *, inventory_id: int, quantity: int, reference: str = "", ): inventory = ( Inventory.objects .select_for_update() .get(pk=inventory_id) ) if quantity <= 0: raise ValueError("Quantity must be greater than zero.") if inventory.reserved_quantity < quantity: raise ValueError( "Cannot release more stock than reserved." ) inventory.reserved_quantity -= quantity inventory.save( update_fields=[ "reserved_quantity", "updated_at", ] ) InventoryTransaction.objects.create( inventory=inventory, transaction_type=InventoryTransactionType.RELEASE, quantity=quantity, reference=reference, note="Inventory reservation released", ) return inventory 9. Completing a sale 

When the order is actually confirmed/fulfilled, the reserved stock becomes sold stock.

Suppose:

quantity = 100 reserved = 20 available = 80 

Customer purchases 5.

We need:

quantity = 95 reserved = 15 available = 80 

Notice something interesting:

Available stock hasn't changed.

That's because the 5 units were already unavailable to other customers because they were reserved.

Code:

@transaction.atomic def commit_sale( *, inventory_id: int, quantity: int, reference: str = "", ): inventory = ( Inventory.objects .select_for_update() .get(pk=inventory_id) ) if inventory.reserved_quantity < quantity: raise ValueError( "Cannot sell more than reserved stock." ) inventory.quantity -= quantity inventory.reserved_quantity -= quantity inventory.save( update_fields=[ "quantity", "reserved_quantity", "updated_at", ] ) InventoryTransaction.objects.create( inventory=inventory, transaction_type=InventoryTransactionType.SALE, quantity=-quantity, reference=reference, note="Reserved inventory converted to sale", ) return inventory 10. Adding new inventory 

Suppose a vendor receives 100 products.

@transaction.atomic def receive_inventory( *, inventory_id: int, quantity: int, reference: str = "", ): inventory = ( Inventory.objects .select_for_update() .get(pk=inventory_id) ) if quantity <= 0: raise ValueError("Quantity must be positive.") inventory.quantity += quantity inventory.save( update_fields=[ "quantity", "updated_at", ] ) InventoryTransaction.objects.create( inventory=inventory, transaction_type=InventoryTransactionType.PURCHASE, quantity=quantity, reference=reference, note="Inventory received", ) return inventory 11. Handling multiple warehouses 

Suppose:

Vendor A Dhaka: iPhone → 20 Khulna: iPhone → 30 

You could query:

Inventory.objects.filter( warehouse__vendor=vendor, variant=variant, ) 

And calculate:

from django.db.models import F, Sum total_quantity = ( Inventory.objects .filter( warehouse__vendor=vendor, variant=variant, ) .aggregate( total=Sum("quantity") )["total"] ) 

But be careful with available inventory.

You can calculate it with an expression:

from django.db.models import F, Sum result = ( Inventory.objects .filter( warehouse__vendor=vendor, variant=variant, ) .aggregate( total=Sum( F("quantity") - F("reserved_quantity") ) ) ) 

Conceptually:

Dhaka: 100 - 20 = 80 Khulna: 50 - 10 = 40 Total available = 120 12. Choosing which warehouse fulfills an order 

A simple strategy could be:

def find_inventory_for_order( *, vendor_id: int, variant_id: int, quantity: int, ): return ( Inventory.objects .filter( warehouse__vendor_id=vendor_id, variant_id=variant_id, quantity__gte=F("reserved_quantity") + quantity, ) .order_by("id") .first() ) 

However, in a real checkout operation, don't rely on this query alone.

You should lock the candidate inventory rows inside a transaction.

For example, conceptually:

@transaction.atomic def reserve_from_vendor( *, vendor_id: int, variant_id: int, quantity: int, reference: str, ): inventories = ( Inventory.objects .select_for_update() .filter( warehouse__vendor_id=vendor_id, variant_id=variant_id, ) .order_by("id") ) remaining = quantity for inventory in inventories: available = inventory.available_quantity if available <= 0: continue to_reserve = min( available, remaining, ) inventory.reserved_quantity += to_reserve inventory.save( update_fields=[ "reserved_quantity", "updated_at", ] ) InventoryTransaction.objects.create( inventory=inventory, transaction_type=( InventoryTransactionType.RESERVATION ), quantity=-to_reserve, reference=reference, ) remaining -= to_reserve if remaining == 0: break if remaining > 0: raise ValueError("Insufficient inventory.") 

This allows an order such as:

Customer wants 15 Dhaka: available = 10 Khulna: available = 8 

to potentially reserve:

Dhaka → 10 Khulna → 5 

although whether you want to split fulfillment this way is a business decision.

13. Inventory transfer 

Suppose:

Dhaka = 100 Khulna = 20 

Move 30 from Dhaka → Khulna.

Do both changes in one transaction:

@transaction.atomic def transfer_inventory( *, source_inventory_id: int, destination_inventory_id: int, quantity: int, reference: str = "", ): if quantity <= 0: raise ValueError("Quantity must be positive.") inventories = ( Inventory.objects .select_for_update() .filter( id__in=[ source_inventory_id, destination_inventory_id, ] ) .order_by("id") ) inventories = list(inventories) if len(inventories) != 2: raise ValueError("Invalid inventory records.") inventory_map = { inventory.id: inventory for inventory in inventories } source = inventory_map[source_inventory_id] destination = inventory_map[destination_inventory_id] if source.available_quantity < quantity: raise ValueError( "Not enough available inventory." ) source.quantity -= quantity destination.quantity += quantity source.save( update_fields=[ "quantity", "updated_at", ] ) destination.save( update_fields=[ "quantity", "updated_at", ] ) InventoryTransaction.objects.create( inventory=source, transaction_type=( InventoryTransactionType.TRANSFER_OUT ), quantity=-quantity, reference=reference, ) InventoryTransaction.objects.create( inventory=destination, transaction_type=( InventoryTransactionType.TRANSFER_IN ), quantity=quantity, reference=reference, ) 

The transaction ensures that you don't get:

Dhaka -30 Khulna +0 

because something failed halfway through.

14. Creating an order 

Now let's put the pieces together.

Imagine the request is:

{ "items": [ { "listing_id": 10, "quantity": 2 }, { "listing_id": 25, "quantity": 1 } ] } 

Listing 10:

Vendor A iPhone ৳90,000 

Listing 25:

Vendor B T-Shirt ৳1,000 

The customer is buying from two vendors.

We can create:

Order #100 │ ├── VendorOrder #100-A │ └── iPhone × 2 │ └── VendorOrder #100-B └── T-Shirt × 1 

A simplified service:

from collections import defaultdict from decimal import Decimal from django.db import transaction from .models import ( Order, OrderItem, OrderStatus, SellerListing, VendorOrder, VendorOrderStatus, ) @transaction.atomic def create_order(*, customer, items): """ items example: [ { "listing_id": 10, "quantity": 2, }, { "listing_id": 25, "quantity": 1, }, ] """ listing_ids = [ item["listing_id"] for item in items ] listings = ( SellerListing.objects .select_for_update() .select_related( "vendor", "variant", ) .filter( id__in=listing_ids, is_active=True, ) ) listing_map = { listing.id: listing for listing in listings } order = Order.objects.create( customer=customer, status=OrderStatus.PENDING, ) vendor_orders = {} total_amount = Decimal("0") for item in items: listing_id = item["listing_id"] quantity = item["quantity"] if quantity <= 0: raise ValueError( "Quantity must be positive." ) listing = listing_map.get(listing_id) if listing is None: raise ValueError( f"Listing {listing_id} does not exist." ) vendor = listing.vendor vendor_order = vendor_orders.get(vendor.id) if vendor_order is None: vendor_order = VendorOrder.objects.create( order=order, vendor=vendor, status=VendorOrderStatus.PENDING, ) vendor_orders[vendor.id] = vendor_order item_total = ( listing.price * quantity ) OrderItem.objects.create( vendor_order=vendor_order, listing=listing, variant=listing.variant, quantity=quantity, unit_price=listing.price, total_price=item_total, ) vendor_order.subtotal += item_total total_amount += item_total for vendor_order in vendor_orders.values(): vendor_order.save( update_fields=[ "subtotal", "updated_at", ] ) order.total_amount = total_amount order.status = OrderStatus.CONFIRMED order.save( update_fields=[ "total_amount", "status", "updated_at", ] ) return order 

However, there is an important missing piece here: inventory reservation.

In a real implementation, you should reserve inventory within this same checkout transaction before confirming the order.

15. A better checkout flow 

I'd structure the application around a service like:

checkout() │ ├── Validate cart │ ├── Lock listings/inventory │ ├── Check stock │ ├── Reserve inventory │ ├── Create Order │ ├── Create VendorOrders │ ├── Create OrderItems │ └── COMMIT 

Something like:

@transaction.atomic def checkout(*, customer, cart_items): # 1. Validate products/listings # 2. Lock inventory rows # SELECT ... FOR UPDATE # 3. Check available stock # 4. Reserve stock # 5. Create Order # 6. Create VendorOrders # 7. Create OrderItems # 8. Commit return order 

That's a much better architecture than putting all of this inside a Django view.

16. Keep business logic out of views 

I would not do this:

class CheckoutView(APIView): def post(self, request): # 300 lines of inventory logic... # 200 lines of order logic... # payment... # shipping... 

Instead:

views.py ↓ services/ ├── checkout.py ├── inventory.py ├── orders.py └── fulfillment.py 

For example:

services/ │ ├── inventory.py │ ├── reserve_inventory() │ ├── release_inventory() │ ├── commit_sale() │ ├── receive_inventory() │ └── transfer_inventory() │ ├── checkout.py │ └── checkout() │ └── orders.py ├── cancel_order() └── complete_order() 

Then the API is very thin:

class CheckoutView(APIView): def post(self, request): order = checkout( customer=request.user, cart_items=request.data["items"], ) return Response({ "order_id": order.id, }) 17. Important database indexes 

Django's ForeignKey fields are indexed automatically, but I'd pay particular attention to the queries you use frequently.

For example:

class Inventory(TimeStampedModel): warehouse = models.ForeignKey(...) variant = models.ForeignKey(...) ... class Meta: constraints = [ models.UniqueConstraint( fields=["warehouse", "variant"], name="unique_warehouse_variant_inventory", ), ] indexes = [ models.Index( fields=["variant"], name="inventory_variant_idx", ), ] 

For listings:

class SellerListing(TimeStampedModel): ... class Meta: constraints = [ models.UniqueConstraint( fields=["vendor", "variant"], name="unique_vendor_variant_listing", ), ] indexes = [ models.Index( fields=["variant", "is_active"], name="listing_variant_active_idx", ), ] 

Don't blindly create dozens of indexes. Add them according to your actual queries.

18. One important improvement: don't let inventory become negative 

At the application level we're already checking:

if available < quantity: raise ValueError(...) 

But for a serious system, you can additionally use a database constraint.

For example:

from django.db.models import F, Q class Inventory(TimeStampedModel): ... class Meta: constraints = [ models.UniqueConstraint( fields=["warehouse", "variant"], name="unique_warehouse_variant_inventory", ), models.CheckConstraint( condition=Q( reserved_quantity__gte=0 ), name="reserved_quantity_non_negative", ), models.CheckConstraint( condition=Q( quantity__gte=0 ), name="quantity_non_negative", ), ] 

You can go further with database-level consistency constraints, but don't assume Django model validation alone protects concurrent transactions. PostgreSQL transactions and row locks are the important part.

19. What Redis should and shouldn't do 

Given the Django + PostgreSQL architecture we've discussed before, I would not use Redis as the source of truth for inventory.

Don't make this:

Redis ↓ stock = 17 

the authoritative inventory.

Instead:

PostgreSQL │ SOURCE OF TRUTH │ ┌──────────┴──────────┐ │ │ Inventory Orders 

Redis can be used for:

Product cache Category cache Popular products Session/cart data Rate limiting Temporary checkout data 

But the authoritative stock should remain in PostgreSQL.

20. The final architecture 

For a small-to-medium Django multi-vendor e-commerce platform, I'd start approximately here:

┌──────────────┐ │ Product │ └──────┬───────┘ │ ┌──────▼───────┐ │ Variant │ │ SKU │ └──────┬───────┘ │ ┌─────────────────┴─────────────────┐ │ │ ┌──────▼───────┐ ┌──────▼───────┐ │ SellerListing│ │ Inventory │ │ │ │ │ │ Vendor │ │ Warehouse │ │ Price │ │ Quantity │ └──────┬───────┘ │ Reserved │ │ └──────┬────────┘ │ │ │ ┌──────▼────────┐ │ │InventoryLedger│ │ └───────────────┘ │ ┌──────▼───────┐ │ Order │ └──────┬───────┘ │ ┌──────▼───────┐ │ VendorOrder │ │ │ │ Vendor │ └──────┬───────┘ │ ┌──────▼───────┐ │ OrderItem │ │ │ │ SKU │ │ Quantity │ │ Price snapshot│ └──────────────┘ The key architectural decision 

For your use case, I would not start with:

Kafka Redis inventory distributed locks sharding multiple PostgreSQL clusters event sourcing everywhere microservices 

I'd start with:

Django + PostgreSQL + transaction.atomic() + select_for_update() + proper constraints/indexes + Redis for caching 

That combination can take you very far.

And the particularly important piece to understand deeply is this:

Inventory correctness is primarily a database concurrency problem, not a caching problem.

Once you understand transaction.atomic(), select_for_update(), inventory reservation, idempotency, and order state transitions, you have the foundation for building a genuinely robust marketplace inventory system.

