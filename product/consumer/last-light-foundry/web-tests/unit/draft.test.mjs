import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  counts, createDraft, createEditor, estimateCost, inBounds, move, occupant, place, redo, sell, toRequest, undo,
} from '../../src/main/resources/web/draft.js';

const floor = { width: 4, height: 3 };

test('placing assigns monotonic installation numbers that are never reused after a sale', () => {
  let editor = createEditor(createDraft());
  editor = place(editor, floor, 'equipment.cutter', 0, 0).editor;
  editor = place(editor, floor, 'equipment.cutter', 1, 0).editor;
  editor = sell(editor, 1).editor;
  const placed = place(editor, floor, 'equipment.assembler', 2, 0);
  assert.equal(placed.number, 3);
  assert.deepEqual(placed.editor.present.installations.map((item) => item.number), [2, 3]);
});

test('moving changes coordinates but never the installation number', () => {
  let editor = createEditor(createDraft([{ number: 7, itemId: 'equipment.cutter', x: 0, y: 0 }]));
  editor = move(editor, floor, 7, 3, 2).editor;
  assert.deepEqual(editor.present.installations, [{ number: 7, itemId: 'equipment.cutter', x: 3, y: 2 }]);
  assert.equal(editor.present.nextNumber, 8);
});

test('placement and movement refuse occupied or out-of-bounds cells without changing the draft', () => {
  const editor = createEditor(createDraft([{ number: 1, itemId: 'a', x: 0, y: 0 }]));
  assert.match(place(editor, floor, 'a', 0, 0).error, /occupied/);
  assert.match(place(editor, floor, 'a', 9, 0).error, /outside/);
  assert.match(move(editor, floor, 1, -1, 0).error, /outside/);
  assert.match(move(editor, floor, 5, 1, 1).error, /Nothing/);
  assert.equal(place(editor, floor, 'a', 9, 0).editor, editor);
  assert.equal(move(editor, floor, 1, 0, 0).editor, editor);
});

test('undo and redo restore exact drafts including identities', () => {
  let editor = createEditor(createDraft());
  editor = place(editor, floor, 'a', 0, 0).editor;
  editor = place(editor, floor, 'b', 1, 0).editor;
  const afterTwo = editor.present;
  editor = undo(editor);
  assert.equal(editor.present.installations.length, 1);
  editor = redo(editor);
  assert.deepEqual(editor.present, afterTwo);
  assert.equal(undo(createEditor(createDraft())).past.length, 0);
  assert.equal(redo(createEditor(createDraft())).future.length, 0);
  // A new edit after undo discards the redo branch.
  editor = undo(editor);
  editor = place(editor, floor, 'c', 2, 2).editor;
  assert.equal(editor.future.length, 0);
  assert.equal(editor.present.installations[1].number, 3, 'a redone-away number is not recycled');
});

test('the request body is independent of the order the draft was listed in', () => {
  const one = createDraft([{ number: 2, itemId: 'b', x: 1, y: 1 }, { number: 1, itemId: 'a', x: 0, y: 0 }]);
  const two = createDraft([{ number: 1, itemId: 'a', x: 0, y: 0 }, { number: 2, itemId: 'b', x: 1, y: 1 }]);
  assert.deepEqual(toRequest(one), toRequest(two));
  assert.deepEqual(toRequest(one).installations.map((item) => item.number), [1, 2]);
});

test('selling an unknown installation is a no-op and counting groups by item', () => {
  const editor = createEditor(createDraft([{ number: 1, itemId: 'a', x: 0, y: 0 }, { number: 2, itemId: 'a', x: 1, y: 0 }]));
  assert.equal(sell(editor, 99).editor, editor);
  assert.deepEqual(counts(editor.present), { a: 2 });
  assert.equal(occupant(editor.present, 1, 0).number, 2);
  assert.equal(occupant(editor.present, 3, 3), null);
  assert.equal(inBounds(floor, 3, 2), true);
  assert.equal(inBounds(floor, 4, 0), false);
});

test('the advisory cost estimate uses exact integers', () => {
  const equipment = [{ itemId: 'a', costCredits: '9007199254740993' }];
  const draft = createDraft([{ number: 1, itemId: 'a', x: 0, y: 0 }, { number: 2, itemId: 'a', x: 1, y: 0 }]);
  assert.equal(estimateCost(draft, equipment), 18014398509481986n);
});
