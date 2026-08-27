import React from "react";

export default function Seal({ size = 36 }) {
  return (
    <div
      className="flex items-center justify-center rounded-full border-2 border-accent text-accent font-display font-semibold select-none"
      style={{ width: size, height: size, fontSize: size * 0.36 }}
    >
      IS
    </div>
  );
}
