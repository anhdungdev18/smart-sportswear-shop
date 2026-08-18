const fs = require('fs');
const path = require('path');

const W = 3508, H = 2480;
const tables = [
  ['promotions',70,245,525,[['PK','id','uuid'],['FK','created_by','uuid?'],['','name','varchar'],['','slug','varchar'],['','type','varchar'],['','scope','varchar'],['','discount_amount','numeric?'],['','discount_percent','numeric?'],['','min_order_amount','numeric?'],['','max_discount_amount','numeric?'],['','starts_at','timestamptz?'],['','ends_at','timestamptz?'],['','status','varchar'],['','usage_limit','integer?'],['','usage_count','integer']]],
  ['promotion_products',650,275,420,[['PK','id','uuid'],['FK','promotion_id','uuid'],['FK','product_id','uuid']]],
  ['coupons',610,650,420,[['PK','id','uuid'],['FK','promotion_id','uuid?'],['','code','varchar'],['','status','varchar'],['','starts_at','timestamptz?'],['','ends_at','timestamptz?'],['','usage_limit','integer?'],['','usage_count','integer'],['','per_user_limit','integer?']]],
  ['coupon_usages',1085,720,430,[['PK','id','uuid'],['FK','coupon_id','uuid'],['FK','order_id','uuid'],['FK','user_id','uuid'],['','discount_amount','numeric'],['','used_at','timestamptz']]],
  ['combos',70,1115,475,[['PK','id','uuid'],['','name','varchar'],['','description','text?'],['','discount_amount','numeric'],['','status','varchar']]],
  ['combo_products',610,1150,420,[['PK','id','uuid'],['FK','combo_id','uuid'],['FK','product_id','uuid'],['','quantity','integer']]],
  ['users',1080,1110,390,[['PK','id','uuid'],['','full_name','varchar'],['','email','varchar']]],
  ['carts',1110,1450,390,[['PK','id','uuid'],['FK','user_id','uuid?'],['','session_id','varchar?']]],
  ['products',1540,660,390,[['PK','id','uuid'],['','name','varchar'],['','slug','varchar'],['','status','varchar']]],
  ['orders',1590,1110,430,[['PK','id','uuid'],['FK','user_id','uuid'],['','order_code','varchar'],['','total_amount','numeric'],['','order_status','varchar'],['','payment_status','varchar']]],
  ['association_rules',2250,270,490,[['PK','id','uuid'],['FK','antecedent_product_id','uuid'],['FK','consequent_product_id','uuid'],['','support','double'],['','confidence','double'],['','lift','double'],['','pair_count','bigint'],['','model_version','varchar'],['','status','varchar']]],
  ['product_embeddings',2200,820,510,[['PK/FK','product_id','uuid'],['','embedding','vector'],['','document_text','text?'],['','embedding_model','varchar?'],['','embedding_dimensions','integer?'],['','status','varchar'],['','last_error','text?']]],
  ['recommendation_logs',2920,350,520,[['PK','id','uuid'],['FK','user_id','uuid?'],['FK','cart_id','uuid?'],['FK','source_product_id','uuid?'],['FK','recommended_product_id','uuid'],['','session_id','varchar?'],['','source_type','varchar'],['','recommendation_type','varchar'],['','event_type','varchar'],['','position_index','integer?'],['','algorithm','varchar'],['','support','double?'],['','confidence','double?'],['','lift','double?'],['','reason','varchar?']]],
  ['banners',70,1840,390,[['PK','id','uuid'],['','name','varchar'],['','code','varchar'],['','placement','varchar'],['','status','varchar'],['','starts_at','timestamptz?'],['','ends_at','timestamptz?']]],
  ['banner_items',510,1840,460,[['PK','id','uuid'],['FK','banner_id','uuid'],['FK','product_id','uuid?'],['','title','varchar?'],['','subtitle','varchar?'],['','image_url','varchar'],['','target_url','varchar?'],['','sort_order','integer'],['','is_active','boolean']]],
  ['pages',1015,1840,400,[['PK','id','uuid'],['FK','created_by','uuid?'],['','title','varchar'],['','slug','varchar'],['','summary','varchar?'],['','status','varchar'],['','published_at','timestamptz?']]],
  ['notifications',1460,1785,465,[['PK','id','uuid'],['FK','user_id','uuid?'],['FK','order_id','uuid?'],['FK','resend_of_id','uuid?'],['','type','varchar'],['','channel','varchar'],['','recipient','varchar'],['','subject','varchar'],['','status','varchar'],['','sent_at','timestamptz?'],['','read_at','timestamptz?'],['','resend_count','integer']]],
  ['notification_templates',2075,1835,450,[['PK','id','uuid'],['','type','varchar'],['','channel','varchar'],['','subject','varchar'],['','body','text']]],
  ['email_logs',2075,2105,470,[['PK','id','uuid'],['','recipient_email','varchar'],['','subject','varchar'],['','template_code','varchar?'],['','status','varchar'],['','provider_message_id','varchar?'],['','error_message','text?'],['','sent_at','timestamptz?']]],
  ['chat_messages',2890,1910,450,[['PK','id','bigint'],['','session_id','text'],['','user_id','text?'],['','role','text'],['','intent','text?'],['','content','text']]],
];

const rowH=36, headH=48;
const box = {};
for (const [name,x,y,w,rows] of tables) box[name]={x,y,w,h:headH+rows.length*rowH+8};
const esc=s=>String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;');
function table(t){const [name,x,y,w,rows]=t; return `<foreignObject x="${x}" y="${y}" width="${w}" height="${headH+rows.length*rowH+8}"><div xmlns="http://www.w3.org/1999/xhtml" class="entity"><div class="ename">${name}</div>${rows.map(r=>`<div class="row"><span class="key">${esc(r[0])}</span><span class="field">${esc(r[1])}</span><span class="type">${esc(r[2])}</span></div>`).join('')}</div></foreignObject>`}
function edge(a,b,label='',dash=false,from='r',to='l') {const A=box[a],B=box[b]; let x1=from==='r'?A.x+A.w:from==='l'?A.x:A.x+A.w/2; let y1=from==='b'?A.y+A.h:from==='t'?A.y:A.y+A.h/2; let x2=to==='l'?B.x:to==='r'?B.x+B.w:B.x+B.w/2; let y2=to==='t'?B.y:to==='b'?B.y+B.h:B.y+B.h/2; const mx=(x1+x2)/2; const d=(from==='b'||from==='t'||to==='b'||to==='t')?`M${x1},${y1} V${(y1+y2)/2} H${x2} V${y2}`:`M${x1},${y1} H${mx} V${y2} H${x2}`; return `<path class="rel${dash?' logical':''}" d="${d}" marker-start="url(#one)" marker-end="url(#many)"/>${label?`<text class="elabel" x="${mx}" y="${(y1+y2)/2-8}">${esc(label)}</text>`:''}`}

let edges='';
edges += edge('promotions','promotion_products');
edges += edge('promotion_products','products');
edges += edge('promotions','coupons');
edges += edge('coupons','coupon_usages');
edges += edge('users','promotions','created_by',false,'t','b');
edges += edge('orders','coupon_usages','order',false,'l','r');
edges += edge('users','coupon_usages','user',false,'r','l');
edges += edge('combos','combo_products');
edges += edge('combo_products','products');
edges += edge('users','orders','',false,'r','l');
edges += edge('users','carts','',false,'b','t');
edges += edge('products','association_rules','antecedent');
edges += edge('products','association_rules','consequent');
edges += edge('products','product_embeddings','1 : 0..1');
edges += edge('products','recommendation_logs','source');
edges += edge('products','recommendation_logs','recommended');
edges += edge('users','recommendation_logs','user',false,'r','l');
edges += edge('carts','recommendation_logs','cart',false,'r','l');
edges += edge('banners','banner_items');
edges += edge('products','banner_items','product',false,'b','t');
edges += edge('users','pages','created_by',false,'b','t');
edges += edge('users','notifications','user',false,'b','t');
edges += edge('orders','notifications','order',false,'b','t');
edges += edge('notifications','notifications','resend_of',false,'r','r');
edges += edge('notifications','notification_templates','logical: type + channel',true);
edges += edge('notification_templates','email_logs','logical: template_code',true,'b','t');
edges += edge('users','chat_messages','logical user reference',true,'b','t');

const svg=`<svg xmlns="http://www.w3.org/2000/svg" width="3508" height="2480" viewBox="0 0 3508 2480">
<defs><style>
text{font-family:Arial,sans-serif;fill:#000}.title{font-size:54px;font-weight:700}.zone{font-size:30px;font-weight:700}.center{font-size:24px;font-weight:700}
.entity{box-sizing:border-box;border:3px solid #000;background:#fff;font:22px Arial,sans-serif;color:#000}.ename{height:${headH}px;box-sizing:border-box;border-bottom:3px solid #000;text-align:center;font-weight:700;font-size:25px;line-height:${headH-3}px}.row{display:grid;grid-template-columns:75px 1fr 145px;height:${rowH}px;line-height:${rowH}px;border-bottom:1.5px solid #000}.row:last-child{border-bottom:0}.row span{box-sizing:border-box;padding:0 9px;white-space:nowrap}.key{font-weight:700;border-right:1.5px solid #000}.field{border-right:1.5px solid #000}.type{text-align:left}.rel{fill:none;stroke:#000;stroke-width:3}.logical{stroke-dasharray:14 10}.elabel{font-size:21px;font-style:italic;paint-order:stroke;stroke:#fff;stroke-width:8;stroke-linejoin:round}.frame{fill:none;stroke:#000;stroke-width:3}
</style><marker id="many" markerWidth="18" markerHeight="18" refX="16" refY="9" orient="auto"><path d="M2,9 L16,2 M2,9 L16,9 M2,9 L16,16" fill="none" stroke="#000" stroke-width="2"/></marker><marker id="one" markerWidth="18" markerHeight="18" refX="1" refY="9" orient="auto"><path d="M3,2 V16 M8,2 V16" stroke="#000" stroke-width="2"/></marker></defs>
<rect width="3508" height="2480" fill="#fff"/><text x="1754" y="95" class="title" text-anchor="middle">SƠ ĐỒ 3.4c. ERD CỤM KHUYẾN MÃI, NỘI DUNG VÀ CHỨC NĂNG THÔNG MINH</text>
<rect class="frame" x="25" y="130" width="2000" height="1580"/><text class="zone" x="55" y="185">1. KHUYẾN MÃI VÀ COMBO</text>
<rect class="frame" x="2050" y="130" width="1433" height="1580"/><text class="zone" x="2080" y="185">3. GỢI Ý SẢN PHẨM VÀ TÌM KIẾM NGỮ NGHĨA</text>
<rect class="frame" x="25" y="1735" width="3458" height="720"/><text class="zone" x="55" y="1790">2. BANNER, NỘI DUNG VÀ THÔNG BÁO</text>
<text class="center" x="1735" y="625" text-anchor="middle">BẢNG TRUNG TÂM</text>
<g>${edges}</g>${tables.map(table).join('')}
<text x="3260" y="2415" font-size="22">? = nullable</text><line x1="3070" y1="2407" x2="3130" y2="2407" stroke="#000" stroke-width="3"/><text x="3142" y="2415" font-size="22">FK</text><line x1="3315" y1="2407" x2="3375" y2="2407" stroke="#000" stroke-width="3" stroke-dasharray="12 8"/><text x="3385" y="2415" font-size="22">logical</text>
</svg>`;
const out=path.join(process.cwd(),'deliverables','erd-3.4c-khuyen-mai-noi-dung-thong-minh.svg');
fs.mkdirSync(path.dirname(out),{recursive:true}); fs.writeFileSync(out,svg,'utf8'); console.log(out);
