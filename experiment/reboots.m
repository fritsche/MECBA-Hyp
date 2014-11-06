 
% reboots.m
% avalia a quantidade de reboots

problems=["OA_AJHotDraw";
"OA_AJHsqldb";
"OA_HealthWatcher";
"OA_TollSystems";
"OO_BCEL";
"OO_JBoss";
"OO_JHotDraw";
"OO_MyBatis"];

rs = zeros(8,31);

for p=1:8
   problem = strcat("MultiArmedBandit/",problems(p,:));
   a = load(strcat(problem, "/REBOOTS.txt"));
   rs(p,:) = a;
endfor

rs = rs';

save("reboots.dat", "rs");