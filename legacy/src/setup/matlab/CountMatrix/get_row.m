function row = get_row (a,b,bin_size,spikes)

x=a:bin_size:b;
row=histc(spikes,x);
return;