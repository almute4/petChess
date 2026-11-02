import { SPRITES, SPRITE_SIZE, type PieceType } from "../pieces";

// Пиксельная шахматная фигура. Рисуется SVG-прямоугольниками без сглаживания,
// поэтому остаётся чёткой при любом размере клетки.
// Заглавная буква - белая фигура, строчная - чёрная (как в FEN): "P", "n", "K"...

const COLORS = {
  white: { fill: "#f0f2c3", detail: "#bcd493", outline: "#153323" },
  black: { fill: "#133526", detail: "#59804b", outline: "#bcd493" },
};

// Склеиваем соседние пиксели строки в одну горизонтальную полосу.
function buildPath(rows: string[], match: (ch: string) => boolean) {
  let d = "";
  rows.forEach((row, y) => {
    let x = 0;
    while (x < row.length) {
      if (match(row[x])) {
        let end = x;
        while (end < row.length && match(row[end])) end++;
        d += `M${x} ${y}h${end - x}v1h${x - end}z`;
        x = end;
      } else {
        x++;
      }
    }
  });
  return d;
}

const pathCache = new Map<PieceType, { body: string; detail: string }>();

function getPaths(type: PieceType) {
  let cached = pathCache.get(type);
  if (!cached) {
    const rows = SPRITES[type];
    cached = {
      body: buildPath(rows, (ch) => ch !== "."),
      detail: buildPath(rows, (ch) => ch === "+"),
    };
    pathCache.set(type, cached);
  }
  return cached;
}

// Клетка 64px = 20 пикселей спрайта по 3px + по 2px отступа с каждой стороны.
const PAD = 2 / 3;
const VIEW = SPRITE_SIZE + PAD * 2;

function Piece({ code }: { code: string }) {
  const type = code.toLowerCase() as PieceType;
  const palette = code === code.toUpperCase() ? COLORS.white : COLORS.black;
  const { body, detail } = getPaths(type);

  return (
    <svg
      className="piece"
      viewBox={`${-PAD} ${-PAD} ${VIEW} ${VIEW}`}
      shapeRendering="crispEdges"
      aria-hidden="true"
    >
      {/* контур: тонкая линия в полпикселя спрайта вокруг силуэта */}
      <path d={body} fill="none" stroke={palette.outline} strokeWidth="1" strokeLinejoin="miter" />
      <path d={body} fill={palette.fill} />
      <path d={detail} fill={palette.detail} />
    </svg>
  );
}

export default Piece;
