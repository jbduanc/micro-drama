import { API_BASE, apiFetch } from "@/lib/api/client";
import { authHeaders } from "@/lib/auth/token";
import type { PaymentOrder } from "@/types";

type CreateOrderResponse = {
  id: string;
  status: string;
};

export async function createWeb3Order(orderId: string): Promise<CreateOrderResponse> {
  return apiFetch<CreateOrderResponse>(API_BASE.payment, "/v1/orders", {
    method: "POST",
    headers: authHeaders(),
    body: { id: orderId },
  });
}

export async function submitSignedTx(signedHex: string): Promise<{ txHash: string }> {
  return apiFetch<{ txHash: string }>(API_BASE.payment, "/v1/tx/raw", {
    method: "POST",
    headers: authHeaders(),
    body: { signedHex },
  });
}

/** Web2 支付占位：后续对接 Stripe / Telegram Stars 等 */
export async function createWeb2Payment(payload: {
  episodeId: string;
  dramaTitle: string;
  episodeTitle: string;
  amount: number;
}): Promise<PaymentOrder> {
  const order: PaymentOrder = {
    id: crypto.randomUUID(),
    status: "paid",
    amount: payload.amount,
    dramaTitle: payload.dramaTitle,
    episodeTitle: payload.episodeTitle,
    method: "web2",
    createdAt: new Date().toISOString(),
  };
  return order;
}

export async function createWeb3Payment(payload: {
  episodeId: string;
  dramaTitle: string;
  episodeTitle: string;
  amount: number;
}): Promise<PaymentOrder> {
  const orderId = crypto.randomUUID();
  await createWeb3Order(orderId);
  return {
    id: orderId,
    status: "pending",
    amount: payload.amount,
    dramaTitle: payload.dramaTitle,
    episodeTitle: payload.episodeTitle,
    method: "web3",
    createdAt: new Date().toISOString(),
  };
}
