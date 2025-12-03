<template>
  <span :class="['vintage-icon', `icon-${name}`, size]" :title="title"></span>
</template>

<script setup>
defineProps({
  name: {
    type: String,
    required: true
  },
  size: {
    type: String,
    default: 'medium' // small, medium, large
  },
  title: {
    type: String,
    default: ''
  }
});
</script>

<style scoped>
.vintage-icon {
  display: inline-block;
  position: relative;
  vertical-align: middle;
}

.vintage-icon.small {
  width: 16px;
  height: 16px;
}

.vintage-icon.medium {
  width: 20px;
  height: 20px;
}

.vintage-icon.large {
  width: 24px;
  height: 24px;
}

/* 书本图标 */
.icon-book {
  background: linear-gradient(to right, 
    #5d4e37 0%, #5d4e37 25%, 
    #8b7355 25%, #8b7355 50%,
    #6b5b45 50%, #6b5b45 75%,
    #5d4e37 75%);
  border: 1.5px solid #3a2f24;
  border-radius: 0 2px 2px 0;
  box-shadow: 
    inset 2px 0 2px rgba(212, 175, 55, 0.2),
    1px 1px 2px rgba(0, 0, 0, 0.4);
}

.icon-book::before {
  content: '';
  position: absolute;
  left: 1px;
  top: 20%;
  bottom: 20%;
  width: 2px;
  background: linear-gradient(to bottom,
    rgba(212, 175, 55, 0.4),
    rgba(139, 115, 85, 0.3),
    rgba(212, 175, 55, 0.4));
}

/* 纸张图标 */
.icon-paper {
  background: linear-gradient(135deg, #fffef9 0%, #f5ede0 100%);
  border: 1.5px solid #d4c5b0;
  border-radius: 2px;
  box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.icon-paper::before {
  content: '';
  position: absolute;
  top: 30%;
  left: 15%;
  right: 15%;
  height: 1px;
  background: rgba(139, 115, 85, 0.2);
  box-shadow: 
    0 3px 0 rgba(139, 115, 85, 0.2),
    0 6px 0 rgba(139, 115, 85, 0.2);
}

/* AI机器人图标 */
.icon-robot {
  background: linear-gradient(135deg, #8b7355 0%, #6b5b45 100%);
  border: 1.5px solid #5d4e37;
  border-radius: 3px;
  box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.icon-robot::before {
  content: '';
  position: absolute;
  top: 25%;
  left: 25%;
  width: 20%;
  height: 20%;
  background: #d4af37;
  border-radius: 50%;
  box-shadow: 
    150% 0 0 #d4af37,
    0 100% 0 1px rgba(212, 175, 55, 0.5),
    150% 100% 0 1px rgba(212, 175, 55, 0.5);
}

.icon-robot::after {
  content: '';
  position: absolute;
  bottom: 20%;
  left: 30%;
  right: 30%;
  height: 2px;
  background: rgba(212, 175, 55, 0.6);
  border-radius: 1px;
}

/* 用户头像图标 */
.icon-user {
  background: linear-gradient(135deg, #f5ede0 0%, #e8dcc8 100%);
  border: 1.5px solid #8b7355;
  border-radius: 50%;
  box-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.icon-user::before {
  content: '';
  position: absolute;
  top: 25%;
  left: 50%;
  transform: translateX(-50%);
  width: 35%;
  height: 35%;
  background: #6b5b45;
  border-radius: 50%;
}

.icon-user::after {
  content: '';
  position: absolute;
  bottom: 15%;
  left: 20%;
  right: 20%;
  height: 40%;
  background: #6b5b45;
  border-radius: 50% 50% 0 0;
}

/* 羽毛笔图标 */
.icon-quill {
  background: transparent;
}

.icon-quill::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) rotate(-30deg);
  width: 3px;
  height: 85%;
  background: linear-gradient(to top, #3a2f24 0%, #8b7355 100%);
  border-radius: 1px;
}

.icon-quill::after {
  content: '';
  position: absolute;
  top: 0;
  right: 20%;
  width: 45%;
  height: 50%;
  background: linear-gradient(135deg, #d4c5b0 0%, #a08968 100%);
  border-radius: 0 80% 20% 0;
  transform: rotate(-20deg);
  box-shadow: -1px 1px 2px rgba(0, 0, 0, 0.2);
}

/* 魔法棒图标 */
.icon-wand {
  background: transparent;
}

.icon-wand::before {
  content: '';
  position: absolute;
  bottom: 10%;
  left: 10%;
  width: 4px;
  height: 70%;
  background: linear-gradient(to top, #5d4e37 0%, #8b7355 100%);
  border-radius: 2px;
  transform: rotate(45deg);
}

.icon-wand::after {
  content: '✦';
  position: absolute;
  top: 0;
  right: 5%;
  font-size: 10px;
  color: #d4af37;
  text-shadow: 0 0 3px rgba(212, 175, 55, 0.6);
}

/* 灯泡图标 */
.icon-bulb {
  background: transparent;
}

.icon-bulb::before {
  content: '';
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, #f5ede0 0%, #d4c5b0 100%);
  border-radius: 50%;
  border: 1.5px solid #8b7355;
  box-shadow: 
    inset 0 -2px 4px rgba(139, 115, 85, 0.3),
    0 0 4px rgba(212, 175, 55, 0.4);
}

.icon-bulb::after {
  content: '';
  position: absolute;
  bottom: 20%;
  left: 50%;
  transform: translateX(-50%);
  width: 30%;
  height: 15%;
  background: #6b5b45;
  border-radius: 2px;
}

/* 闪光图标 */
.icon-sparkle {
  background: transparent;
}

.icon-sparkle::before {
  content: '✦';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 14px;
  color: #d4af37;
  text-shadow: 0 0 4px rgba(212, 175, 55, 0.8);
}

/* 人物图标 */
.icon-person {
  background: transparent;
}

.icon-person::before {
  content: '';
  position: absolute;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  width: 40%;
  height: 40%;
  background: #6b5b45;
  border-radius: 50%;
  border: 1.5px solid #5d4e37;
}

.icon-person::after {
  content: '';
  position: absolute;
  bottom: 15%;
  left: 25%;
  right: 25%;
  height: 45%;
  background: linear-gradient(to bottom, #6b5b45 0%, #5d4e37 100%);
  border-radius: 40% 40% 0 0;
  border: 1.5px solid #5d4e37;
  border-bottom: none;
}

/* 空状态图标 */
.icon-empty {
  background: transparent;
  width: 48px;
  height: 48px;
}

.icon-empty::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-5deg);
  width: 70%;
  height: 85%;
  background: linear-gradient(135deg, #fffef9 0%, #f5ede0 100%);
  border: 2px solid #d4c5b0;
  border-radius: 3px;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.icon-empty::after {
  content: '';
  position: absolute;
  top: 30%;
  left: 25%;
  right: 25%;
  height: 2px;
  background: rgba(139, 115, 85, 0.2);
  box-shadow: 
    0 6px 0 rgba(139, 115, 85, 0.2),
    0 12px 0 rgba(139, 115, 85, 0.2),
    0 18px 0 rgba(139, 115, 85, 0.2);
}
</style>
