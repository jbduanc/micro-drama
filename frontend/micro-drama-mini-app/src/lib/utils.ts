import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatPrice(value?: number | string | null): string {
  if (value == null || value === "") return "免费";
  const num = typeof value === "string" ? Number(value) : value;
  if (Number.isNaN(num) || num <= 0) return "免费";
  return `$${num.toFixed(2)}`;
}

export function formatDuration(seconds?: number | null): string {
  if (!seconds || seconds <= 0) return "--:--";
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${String(s).padStart(2, "0")}`;
}
