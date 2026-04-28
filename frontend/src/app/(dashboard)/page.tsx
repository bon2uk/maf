import Link from "next/link";
import { getCurrentUser } from "@/domains/user/infrastructure/server/user-server-api";
import { getProducts } from "@/domains/product/infrastructure/server/product-server-api";
import { PageHeader } from "@/shared/components/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Package, TrendingUp, Users, ShoppingCart, Plus } from "lucide-react";

export default async function DashboardPage() {
  const [user, productsData] = await Promise.all([
    getCurrentUser(),
    getProducts({ size: 5 }),
  ]);

  const activeCount = productsData.items.filter((p) => p.status === "ACTIVE").length;
  const lowStockCount = 0; // Stock is not part of the current Product model.
  const categoriesCount = 0; // Categories are not part of the current Product model.

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome back, ${user.firstName || "User"}`}
        description="Here's an overview of your dashboard"
      />

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Products</CardTitle>
            <Package className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{productsData.total}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Products</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{activeCount}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Low Stock Items</CardTitle>
            <ShoppingCart className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{lowStockCount}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Categories</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{categoriesCount}</div>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle>Recent Products</CardTitle>
            <Button asChild size="sm">
              <Link href="/products/new">
                <Plus className="mr-2 h-4 w-4" />
                Add Product
              </Link>
            </Button>
          </CardHeader>
          <CardContent>
            {productsData.items.length === 0 ? (
              <p className="text-muted-foreground text-sm">No products yet</p>
            ) : (
              <div className="space-y-3">
                {productsData.items.slice(0, 5).map((product) => (
                  <Link
                    key={product.id}
                    href={`/products/${product.id}`}
                    className="flex items-center justify-between p-3 rounded-lg border hover:bg-muted/50 transition-colors"
                  >
                    <div>
                      <p className="font-medium">{product.name}</p>
                      <p className="text-sm text-muted-foreground">{product.status}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium">${product.price.toFixed(2)}</p>
                      <p className="text-sm text-muted-foreground">{product.currency}</p>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button asChild className="w-full justify-start" variant="outline">
              <Link href="/products/new">
                <Plus className="mr-2 h-4 w-4" />
                Create New Product
              </Link>
            </Button>
            <Button asChild className="w-full justify-start" variant="outline">
              <Link href="/products">
                <Package className="mr-2 h-4 w-4" />
                View All Products
              </Link>
            </Button>
            <Button asChild className="w-full justify-start" variant="outline">
              <Link href="/profile">
                <Users className="mr-2 h-4 w-4" />
                Edit Profile
              </Link>
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
