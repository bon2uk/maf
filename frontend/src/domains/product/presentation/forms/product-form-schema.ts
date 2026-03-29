import { z } from "zod";

export const productFormSchema = z.object({
  name: z.string().min(1, "Product name is required").max(100, "Name is too long"),
  description: z.string().min(1, "Description is required").max(1000, "Description is too long"),
  price: z.coerce.number().min(0.01, "Price must be greater than 0"),
  currency: z.enum(["USD", "EUR", "GBP", "PLN"]),
});

export type ProductFormValues = z.infer<typeof productFormSchema>;
