// Pure draft-editing logic: no DOM, no network. The draft is the player's *untrusted* proposal;
// the Java server recomputes cost, admissibility, projection and publication from it every time.
//
// Each installation has a stable `number` (its identity, which the server maps to the Factory
// resource identity) and cosmetic `x`/`y` coordinates. Numbers come from a monotonic counter, are
// never reused or renumbered by edits, and survive undo/redo. Moving never changes a number.

/** @typedef {{number:number,itemId:string,x:number,y:number}} Installation */
/** @typedef {{installations:Installation[],nextNumber:number}} DraftState */

/** @returns {DraftState} */
export function createDraft(installations = []) {
  const nextNumber = installations.reduce((max, item) => Math.max(max, item.number), 0) + 1;
  return { installations: installations.map((item) => ({ ...item })), nextNumber };
}

/** Undoable editor history wrapping a {@link DraftState}. */
export function createEditor(initial) {
  return { present: initial, past: [], future: [] };
}

function commit(editor, next) {
  return { present: next, past: [...editor.past, editor.present], future: [] };
}

export function occupant(draft, x, y) {
  return draft.installations.find((item) => item.x === x && item.y === y) ?? null;
}

export function inBounds(floor, x, y) {
  return Number.isInteger(x) && Number.isInteger(y) && x >= 0 && y >= 0 && x < floor.width && y < floor.height;
}

/** Places a new installation; returns `{editor, error?, number?}`. */
export function place(editor, floor, itemId, x, y) {
  if (!inBounds(floor, x, y)) return { editor, error: 'That cell is outside the floor.' };
  if (occupant(editor.present, x, y)) return { editor, error: 'That cell is already occupied.' };
  const number = editor.present.nextNumber;
  const next = {
    installations: [...editor.present.installations, { number, itemId, x, y }],
    nextNumber: number + 1,
  };
  return { editor: commit(editor, next), number };
}

/** Moves an installation to a free cell; its number never changes. */
export function move(editor, floor, number, x, y) {
  const item = editor.present.installations.find((candidate) => candidate.number === number);
  if (!item) return { editor, error: 'Nothing is selected.' };
  if (!inBounds(floor, x, y)) return { editor, error: 'That cell is outside the floor.' };
  if (item.x === x && item.y === y) return { editor };
  if (occupant(editor.present, x, y)) return { editor, error: 'That cell is already occupied.' };
  const next = {
    ...editor.present,
    installations: editor.present.installations.map((candidate) =>
      candidate.number === number ? { ...candidate, x, y } : candidate),
  };
  return { editor: commit(editor, next) };
}

/** Sells (removes) an installation. Its number is retired, never reused. */
export function sell(editor, number) {
  if (!editor.present.installations.some((item) => item.number === number)) return { editor };
  const next = {
    ...editor.present,
    installations: editor.present.installations.filter((item) => item.number !== number),
  };
  return { editor: commit(editor, next) };
}

// The installation-number counter only ever moves forward, even across undo and redo, so a number
// that has been issued is never handed to a different machine.
function keepCounter(target, current) {
  return { ...target, nextNumber: Math.max(target.nextNumber, current.nextNumber) };
}

export function undo(editor) {
  if (editor.past.length === 0) return editor;
  const previous = keepCounter(editor.past[editor.past.length - 1], editor.present);
  return { present: previous, past: editor.past.slice(0, -1), future: [editor.present, ...editor.future] };
}

export function redo(editor) {
  if (editor.future.length === 0) return editor;
  const [target, ...rest] = editor.future;
  const next = keepCounter(target, editor.present);
  return { present: next, past: [...editor.past, editor.present], future: rest };
}

/** The request body the server expects: installations only, ordered by number. */
export function toRequest(draft) {
  const installations = [...draft.installations]
    .sort((a, b) => a.number - b.number)
    .map(({ number, itemId, x, y }) => ({ number, itemId, x, y }));
  return { installations };
}

/** How many of each catalogue item the draft holds. */
export function counts(draft) {
  const result = {};
  for (const item of draft.installations) result[item.itemId] = (result[item.itemId] ?? 0) + 1;
  return result;
}

/** Local, advisory spend estimate; the server's economics report is authoritative. */
export function estimateCost(draft, equipment) {
  const price = new Map(equipment.map((entry) => [entry.itemId, BigInt(entry.costCredits)]));
  return draft.installations.reduce((sum, item) => sum + (price.get(item.itemId) ?? 0n), 0n);
}
