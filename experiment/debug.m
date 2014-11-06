 
% debug.m
% avalia os valores de q e aux.

% sed -i 's/Infinity/Inf/g' EXECUTION_*/DEBUG_aux.txt

EXPERIMENTS = 30;

problems=["OA_AJHotDraw";
"OA_AJHsqldb";
"OA_HealthWatcher";
"OA_TollSystems";
"OO_BCEL";
"OO_JBoss";
"OO_JHotDraw";
"OO_MyBatis"];

for p=1:8
  qs = zeros(EXPERIMENTS,12450,6);
  as = zeros(EXPERIMENTS,12450,6);

  for i = 1:EXPERIMENTS
    problem = strcat("MultiArmedBandit/",problems(p,:));
    problem = strcat(problem, "/EXECUTION_");
    path = strcat(problem,  num2str(i-1));
    a = load(strcat(path,"/DEBUG_aux.txt"));
    q = load(strcat(path,"/DEBUG_q.txt"));
    as(i,:,:) = a; 
    qs(i,:,:) = q;
  endfor

  av = mean(mean(as,3))';
  qv = mean(mean(qs,3))';

  save(strcat(problems(p,:),"_av.dat"), "av");
  save(strcat(problems(p,:),"_qv.dat"), "qv");
endfor