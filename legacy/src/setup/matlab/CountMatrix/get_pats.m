function pats = get_pats (M,width)
last_col = size (M,2)-width+1;
pat_size = width*10;
pats=[];
for i=1:last_col,
    pat = reshape (M(:,i:i+width-1)',1,pat_size);
    pats=[pats; pat ];
end
return;